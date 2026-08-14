package com.warrantywise.mapper;

import com.warrantywise.dto.warranty.WarrantyRequest;
import com.warrantywise.dto.warranty.WarrantyResponse;
import com.warrantywise.entity.Product;
import com.warrantywise.entity.Warranty;
import com.warrantywise.enums.WarrantyStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class WarrantyMapper {

    public WarrantyResponse toResponse(Warranty warranty, Long attachmentCount) {
        if (warranty == null) {
            return null;
        }

        Long productId = warranty.getProduct() != null ? warranty.getProduct().getId() : null;
        String productName = warranty.getProduct() != null ? warranty.getProduct().getName() : null;
        String productModelName = warranty.getProduct() != null ? warranty.getProduct().getModelName() : null;

        LocalDate today = LocalDate.now();
        Long daysRemaining = null;
        if (warranty.getEndDate() != null) {
            daysRemaining = ChronoUnit.DAYS.between(today, warranty.getEndDate());
        }

        boolean isExpired = warranty.getStatus() == WarrantyStatus.EXPIRED
                || (warranty.getEndDate() != null && today.isAfter(warranty.getEndDate()));

        boolean isExpiringSoon = warranty.getStatus() == WarrantyStatus.EXPIRING_SOON
                || (!isExpired && daysRemaining != null && daysRemaining <= 30);

        return WarrantyResponse.builder()
                .id(warranty.getId())
                .productId(productId)
                .productName(productName)
                .productModelName(productModelName)
                .warrantyType(warranty.getWarrantyType())
                .provider(warranty.getProvider())
                .startDate(warranty.getStartDate())
                .endDate(warranty.getEndDate())
                .coverageDetails(warranty.getCoverageDetails())
                .termsAndConditions(warranty.getTermsAndConditions())
                .status(warranty.getStatus())
                .daysRemaining(daysRemaining)
                .isExpired(isExpired)
                .isExpiringSoon(isExpiringSoon)
                .attachmentCount(attachmentCount != null ? attachmentCount : 0L)
                .createdAt(warranty.getCreatedAt())
                .updatedAt(warranty.getUpdatedAt())
                .build();
    }

    public Warranty toEntity(WarrantyRequest request, Product product) {
        if (request == null) {
            return null;
        }

        return Warranty.builder()
                .product(product)
                .warrantyType(request.getWarrantyType())
                .provider(request.getProvider())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .coverageDetails(request.getCoverageDetails())
                .termsAndConditions(request.getTermsAndConditions())
                .status(request.getStatus() != null ? request.getStatus() : WarrantyStatus.ACTIVE)
                .build();
    }

    public void updateEntityFromRequest(WarrantyRequest request, Warranty warranty, Product product) {
        if (request == null || warranty == null) {
            return;
        }

        if (product != null) {
            warranty.setProduct(product);
        }
        if (request.getWarrantyType() != null) {
            warranty.setWarrantyType(request.getWarrantyType());
        }
        if (request.getProvider() != null) {
            warranty.setProvider(request.getProvider());
        }
        if (request.getStartDate() != null) {
            warranty.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            warranty.setEndDate(request.getEndDate());
        }
        if (request.getCoverageDetails() != null) {
            warranty.setCoverageDetails(request.getCoverageDetails());
        }
        if (request.getTermsAndConditions() != null) {
            warranty.setTermsAndConditions(request.getTermsAndConditions());
        }
        if (request.getStatus() != null) {
            warranty.setStatus(request.getStatus());
        }
    }
}
