package com.warrantywise.repository;

import com.warrantywise.entity.Attachment;
import com.warrantywise.enums.AttachmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<Attachment> findByEntityTypeAndEntityIdAndAttachmentType(String entityType, Long entityId, AttachmentType attachmentType);

    void deleteByEntityTypeAndEntityId(String entityType, Long entityId);

    long countByEntityTypeAndEntityId(String entityType, Long entityId);

    Page<Attachment> findByUploadedById(Long userId, Pageable pageable);

    Page<Attachment> findByUploadedByIdAndFileNameContainingIgnoreCase(Long userId, String fileName, Pageable pageable);

    Page<Attachment> findByUploadedByIdAndAttachmentType(Long userId, AttachmentType attachmentType, Pageable pageable);

    Page<Attachment> findByUploadedByIdAndFileNameContainingIgnoreCaseAndAttachmentType(Long userId, String fileName, AttachmentType attachmentType, Pageable pageable);
}
