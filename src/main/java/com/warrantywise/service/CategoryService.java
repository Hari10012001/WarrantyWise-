package com.warrantywise.service;

import com.warrantywise.dto.category.CategoryRequest;
import com.warrantywise.dto.category.CategoryResponse;
import com.warrantywise.security.UserPrincipal;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request, String ipAddress, UserPrincipal currentUser);
    CategoryResponse updateCategory(Long id, CategoryRequest request, String ipAddress, UserPrincipal currentUser);
    void deleteCategory(Long id, String ipAddress, UserPrincipal currentUser);
    CategoryResponse getCategoryById(Long id);
    List<CategoryResponse> getAllActiveCategories();
    Page<CategoryResponse> getAllCategories(int page, int size, String sortBy, String sortDir, String search, Boolean isActive);
}
