package com.warrantywise.controller;

import com.warrantywise.dto.attachment.AttachmentResponse;
import com.warrantywise.enums.AttachmentType;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.AttachmentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId,
            @RequestParam("attachmentType") AttachmentType attachmentType,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        
        String ipAddress = httpRequest.getRemoteAddr();
        AttachmentResponse response = attachmentService.uploadAttachment(file, entityType, entityId, attachmentType, ipAddress, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
            
        Resource resource = attachmentService.downloadAttachment(id, currentUser);
        AttachmentResponse metadata = attachmentService.getAttachmentMetadata(id, currentUser);
        
        String contentType = metadata.getFileType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttachmentResponse> getAttachmentMetadata(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
            
        AttachmentResponse response = attachmentService.getAttachmentMetadata(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
            
        String ipAddress = httpRequest.getRemoteAddr();
        attachmentService.deleteAttachment(id, ipAddress, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AttachmentResponse>> getAttachmentsForEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
            
        List<AttachmentResponse> responses = attachmentService.getAttachmentsForEntity(entityType, entityId, currentUser);
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    public ResponseEntity<Page<AttachmentResponse>> getUserAttachmentsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AttachmentType attachmentType,
            @AuthenticationPrincipal UserPrincipal currentUser) {
            
        Page<AttachmentResponse> responses = attachmentService.getUserAttachmentsPaginated(page, size, sortBy, sortDir, search, attachmentType, currentUser);
        return ResponseEntity.ok(responses);
    }
}
