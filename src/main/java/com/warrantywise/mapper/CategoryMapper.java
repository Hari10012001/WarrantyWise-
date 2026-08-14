package com.warrantywise.mapper;

import com.warrantywise.dto.category.CategoryRequest;
import com.warrantywise.dto.category.CategoryResponse;
import com.warrantywise.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequest request) {
        if (request == null) {
            return null;
        }

        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIcon())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
    }

    public CategoryResponse toResponse(Category category, Long productCount) {
        if (category == null) {
            return null;
        }

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .icon(category.getIcon())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .productCount(productCount != null ? productCount : 0L)
                .build();
    }

    public void updateEntityFromRequest(CategoryRequest request, Category category) {
        if (request == null || category == null) {
            return;
        }

        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }
    }
}
