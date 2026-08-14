package com.warrantywise.mapper;

import com.warrantywise.dto.servicerecord.ServiceRecordRequest;
import com.warrantywise.dto.servicerecord.ServiceRecordResponse;
import com.warrantywise.entity.Product;
import com.warrantywise.entity.ServiceRecord;
import org.springframework.stereotype.Component;

@Component
public class ServiceRecordMapper {

    public ServiceRecordResponse toResponse(ServiceRecord record, Long attachmentCount) {
        if (record == null) {
            return null;
        }

        Long productId = record.getProduct() != null ? record.getProduct().getId() : null;
        String productName = record.getProduct() != null ? record.getProduct().getName() : null;
        String productModelName = record.getProduct() != null ? record.getProduct().getModelName() : null;

        return ServiceRecordResponse.builder()
                .id(record.getId())
                .productId(productId)
                .productName(productName)
                .productModelName(productModelName)
                .serviceType(record.getServiceType())
                .serviceProvider(record.getServiceProvider())
                .serviceDate(record.getServiceDate())
                .completionDate(record.getCompletionDate())
                .cost(record.getCost())
                .description(record.getDescription())
                .workPerformed(record.getWorkPerformed())
                .partsReplaced(record.getPartsReplaced())
                .nextServiceDate(record.getNextServiceDate())
                .serviceStatus(record.getServiceStatus())
                .notes(record.getNotes())
                .attachmentCount(attachmentCount != null ? attachmentCount : 0L)
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    public ServiceRecord toEntity(ServiceRecordRequest request, Product product) {
        if (request == null) {
            return null;
        }

        return ServiceRecord.builder()
                .product(product)
                .serviceDate(request.getServiceDate())
                .serviceType(request.getServiceType())
                .serviceProvider(request.getServiceProvider())
                .description(request.getDescription())
                .cost(request.getCost())
                .nextServiceDate(request.getNextServiceDate())
                .completionDate(request.getCompletionDate())
                .workPerformed(request.getWorkPerformed())
                .partsReplaced(request.getPartsReplaced())
                .serviceStatus(request.getServiceStatus() != null ? request.getServiceStatus() : "COMPLETED")
                .notes(request.getNotes())
                .build();
    }

    public void updateEntityFromRequest(ServiceRecordRequest request, ServiceRecord record, Product product) {
        if (request == null || record == null) {
            return;
        }

        if (product != null) {
            record.setProduct(product);
        }
        if (request.getServiceDate() != null) {
            record.setServiceDate(request.getServiceDate());
        }
        if (request.getServiceType() != null) {
            record.setServiceType(request.getServiceType());
        }
        if (request.getServiceProvider() != null) {
            record.setServiceProvider(request.getServiceProvider());
        }
        if (request.getDescription() != null) {
            record.setDescription(request.getDescription());
        }
        if (request.getCost() != null) {
            record.setCost(request.getCost());
        }
        if (request.getNextServiceDate() != null) {
            record.setNextServiceDate(request.getNextServiceDate());
        }
        if (request.getCompletionDate() != null) {
            record.setCompletionDate(request.getCompletionDate());
        }
        if (request.getWorkPerformed() != null) {
            record.setWorkPerformed(request.getWorkPerformed());
        }
        if (request.getPartsReplaced() != null) {
            record.setPartsReplaced(request.getPartsReplaced());
        }
        if (request.getServiceStatus() != null) {
            record.setServiceStatus(request.getServiceStatus());
        }
        if (request.getNotes() != null) {
            record.setNotes(request.getNotes());
        }
    }
}
