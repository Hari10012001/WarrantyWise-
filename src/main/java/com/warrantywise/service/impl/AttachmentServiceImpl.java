package com.warrantywise.service.impl;

import com.warrantywise.dto.attachment.AttachmentResponse;
import com.warrantywise.entity.Attachment;
import com.warrantywise.entity.User;
import com.warrantywise.enums.ActionType;
import com.warrantywise.enums.AttachmentType;
import com.warrantywise.exception.BadRequestException;
import com.warrantywise.exception.ResourceNotFoundException;
import com.warrantywise.exception.UnauthorizedException;
import com.warrantywise.mapper.AttachmentMapper;
import com.warrantywise.repository.AttachmentRepository;
import com.warrantywise.repository.UserRepository;
import com.warrantywise.repository.ActivityLogRepository;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.AttachmentService;
import com.warrantywise.service.FileStorageService;
import com.warrantywise.entity.ActivityLog;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final FileStorageService fileStorageService;
    private final AttachmentMapper attachmentMapper;

    public AttachmentServiceImpl(AttachmentRepository attachmentRepository,
                                 UserRepository userRepository,
                                 ActivityLogRepository activityLogRepository,
                                 FileStorageService fileStorageService,
                                 AttachmentMapper attachmentMapper) {
        this.attachmentRepository = attachmentRepository;
        this.userRepository = userRepository;
        this.activityLogRepository = activityLogRepository;
        this.fileStorageService = fileStorageService;
        this.attachmentMapper = attachmentMapper;
    }

    @Override
    public AttachmentResponse uploadAttachment(MultipartFile file, String entityType, Long entityId, AttachmentType attachmentType, String ipAddress, UserPrincipal currentUser) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Cannot upload empty file");
        }
        
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BadRequestException("File size exceeds maximum limit of 5MB");
        }
        
        String mimeType = file.getContentType();
        List<String> allowedMimeTypes = List.of(
            "image/jpeg", 
            "image/png", 
            "image/webp",
            "application/pdf", 
            "application/msword", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );
        
        if (!allowedMimeTypes.contains(mimeType)) {
            throw new BadRequestException("Invalid MIME type: " + mimeType);
        }
        
        String storedFileName = fileStorageService.storeFile(file);
        
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));
                
        Attachment attachment = new Attachment();
        attachment.setFileName(file.getOriginalFilename());
        attachment.setStoredName(storedFileName);
        attachment.setFilePath("/uploads/" + storedFileName);
        attachment.setFileType(mimeType);
        attachment.setFileSize(file.getSize());
        attachment.setEntityType(entityType);
        attachment.setEntityId(entityId);
        attachment.setAttachmentType(attachmentType);
        attachment.setUploadedBy(user);
        
        Attachment saved = attachmentRepository.save(attachment);
        
        ActivityLog log = ActivityLog.builder()
                .user(user)
                .action(ActionType.UPLOAD)
                .entityType("ATTACHMENT")
                .entityId(saved.getId())
                .description("Uploaded file: " + saved.getFileName())
                .ipAddress(ipAddress)
                .build();
        activityLogRepository.save(log);
        
        return attachmentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadAttachment(Long id, UserPrincipal currentUser) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", id));
                
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = attachment.getUploadedBy().getId().equals(currentUser.getId());
        
        if (!isAdmin && !isOwner) {
            throw new UnauthorizedException("Access denied");
        }
        
        return fileStorageService.loadFileAsResource(attachment.getStoredName());
    }

    @Override
    @Transactional(readOnly = true)
    public AttachmentResponse getAttachmentMetadata(Long id, UserPrincipal currentUser) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", id));
                
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = attachment.getUploadedBy().getId().equals(currentUser.getId());
        
        if (!isAdmin && !isOwner) {
            throw new UnauthorizedException("Access denied");
        }
        
        return attachmentMapper.toResponse(attachment);
    }

    @Override
    public void deleteAttachment(Long id, String ipAddress, UserPrincipal currentUser) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", id));
                
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = attachment.getUploadedBy().getId().equals(currentUser.getId());
        
        if (!isAdmin && !isOwner) {
            throw new UnauthorizedException("Access denied");
        }
        
        fileStorageService.deleteFile(attachment.getStoredName());
        attachmentRepository.delete(attachment);
        
        User user = userRepository.findById(currentUser.getId()).orElse(null);
        
        if (user != null) {
            ActivityLog log = ActivityLog.builder()
                    .user(user)
                    .action(ActionType.DELETE)
                    .entityType("ATTACHMENT")
                    .entityId(id)
                    .description("Deleted file: " + attachment.getFileName())
                    .ipAddress(ipAddress)
                    .build();
            activityLogRepository.save(log);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachmentsForEntity(String entityType, Long entityId, UserPrincipal currentUser) {
        List<Attachment> attachments = attachmentRepository.findByEntityTypeAndEntityId(entityType, entityId);
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        return attachments.stream()
                .filter(a -> isAdmin || (a.getUploadedBy() != null && a.getUploadedBy().getId().equals(currentUser.getId())))
                .map(attachmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttachmentResponse> getUserAttachmentsPaginated(int page, int size, String sortBy, String sortDir, String search, AttachmentType attachmentType, UserPrincipal currentUser) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Attachment> attachmentPage;
        boolean hasSearch = search != null && !search.trim().isEmpty();
        
        if (hasSearch && attachmentType != null) {
            attachmentPage = attachmentRepository.findByUploadedByIdAndFileNameContainingIgnoreCaseAndAttachmentType(currentUser.getId(), search.trim(), attachmentType, pageable);
        } else if (hasSearch) {
            attachmentPage = attachmentRepository.findByUploadedByIdAndFileNameContainingIgnoreCase(currentUser.getId(), search.trim(), pageable);
        } else if (attachmentType != null) {
            attachmentPage = attachmentRepository.findByUploadedByIdAndAttachmentType(currentUser.getId(), attachmentType, pageable);
        } else {
            attachmentPage = attachmentRepository.findByUploadedById(currentUser.getId(), pageable);
        }
        
        return attachmentPage.map(attachmentMapper::toResponse);
    }
}
