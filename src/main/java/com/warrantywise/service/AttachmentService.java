package com.warrantywise.service;

import com.warrantywise.dto.attachment.AttachmentResponse;
import com.warrantywise.enums.AttachmentType;
import com.warrantywise.security.UserPrincipal;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {
    AttachmentResponse uploadAttachment(MultipartFile file, String entityType, Long entityId, AttachmentType attachmentType, String ipAddress, UserPrincipal currentUser);
    Resource downloadAttachment(Long id, UserPrincipal currentUser);
    AttachmentResponse getAttachmentMetadata(Long id, UserPrincipal currentUser);
    void deleteAttachment(Long id, String ipAddress, UserPrincipal currentUser);
    List<AttachmentResponse> getAttachmentsForEntity(String entityType, Long entityId, UserPrincipal currentUser);
    Page<AttachmentResponse> getUserAttachmentsPaginated(int page, int size, String sortBy, String sortDir, String search, AttachmentType attachmentType, UserPrincipal currentUser);
}
