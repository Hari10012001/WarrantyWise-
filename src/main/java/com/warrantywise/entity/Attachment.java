package com.warrantywise.entity;

import com.warrantywise.enums.AttachmentType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attachments", indexes = {@Index(name="idx_attachments_entity", columnList="entity_type, entity_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "stored_name", length = 255, nullable = false)
    private String storedName;

    @Column(name = "file_path", length = 500, nullable = false)
    private String filePath;

    @Column(name = "file_type", length = 100)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "entity_type", length = 50, nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_type", length = 20)
    private AttachmentType attachmentType;
}
