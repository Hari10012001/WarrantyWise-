package com.warrantywise.dto.attachment;

import com.warrantywise.dto.auth.UserSummaryResponse;
import com.warrantywise.enums.AttachmentType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResponse {
    private Long id;
    private String fileName;
    private String storedName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String entityType;
    private Long entityId;
    private AttachmentType attachmentType;
    private UserSummaryResponse uploadedBy;
    private LocalDateTime createdAt;
}
