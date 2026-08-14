package com.warrantywise.mapper;

import com.warrantywise.dto.brand.BrandRequest;
import com.warrantywise.dto.brand.BrandResponse;
import com.warrantywise.entity.Brand;
import org.springframework.stereotype.Component;

@Component
public class BrandMapper {

    public Brand toEntity(BrandRequest request) {
        if (request == null) {
            return null;
        }

        return Brand.builder()
                .name(request.getName())
                .logoUrl(request.getLogoUrl())
                .website(request.getWebsite())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
    }

    public BrandResponse toResponse(Brand brand, Long productCount) {
        if (brand == null) {
            return null;
        }

        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .logoUrl(brand.getLogoUrl())
                .website(brand.getWebsite())
                .isActive(brand.getIsActive())
                .createdAt(brand.getCreatedAt())
                .updatedAt(brand.getUpdatedAt())
                .productCount(productCount != null ? productCount : 0L)
                .build();
    }

    public void updateEntityFromRequest(BrandRequest request, Brand brand) {
        if (request == null || brand == null) {
            return;
        }

        if (request.getName() != null) {
            brand.setName(request.getName());
        }
        if (request.getLogoUrl() != null) {
            brand.setLogoUrl(request.getLogoUrl());
        }
        if (request.getWebsite() != null) {
            brand.setWebsite(request.getWebsite());
        }
        if (request.getIsActive() != null) {
            brand.setIsActive(request.getIsActive());
        }
    }
}
