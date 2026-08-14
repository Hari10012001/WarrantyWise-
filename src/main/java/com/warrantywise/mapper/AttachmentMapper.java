package com.warrantywise.mapper;

import com.warrantywise.dto.attachment.AttachmentResponse;
import com.warrantywise.dto.auth.UserSummaryResponse;
import com.warrantywise.entity.Attachment;
import com.warrantywise.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AttachmentMapper {

    public AttachmentResponse toResponse(Attachment attachment) {
        if (attachment == null) {
            return null;
        }

        UserSummaryResponse userSummary = null;
        if (attachment.getUploadedBy() != null) {
            User user = attachment.getUploadedBy();
            userSummary = UserSummaryResponse.builder()
                    .id(user.getId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .phone(user.getPhone())
                    .build();
        }

        return AttachmentResponse.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .storedName(attachment.getStoredName())
                .filePath(attachment.getFilePath())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .entityType(attachment.getEntityType())
                .entityId(attachment.getEntityId())
                .attachmentType(attachment.getAttachmentType())
                .uploadedBy(userSummary)
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}
