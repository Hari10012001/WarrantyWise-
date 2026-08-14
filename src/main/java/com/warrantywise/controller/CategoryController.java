package com.warrantywise.controller;

import com.warrantywise.dto.category.CategoryRequest;
import com.warrantywise.dto.category.CategoryResponse;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request,
                                                           @AuthenticationPrincipal UserPrincipal currentUser,
                                                           HttpServletRequest httpRequest) {
        CategoryResponse response = categoryService.createCategory(request, httpRequest.getRemoteAddr(), currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id,
                                                           @Valid @RequestBody CategoryRequest request,
                                                           @AuthenticationPrincipal UserPrincipal currentUser,
                                                           HttpServletRequest httpRequest) {
        CategoryResponse response = categoryService.updateCategory(id, request, httpRequest.getRemoteAddr(), currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id,
                                               @AuthenticationPrincipal UserPrincipal currentUser,
                                               HttpServletRequest httpRequest) {
        categoryService.deleteCategory(id, httpRequest.getRemoteAddr(), currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<CategoryResponse>> getAllActiveCategories() {
        List<CategoryResponse> responses = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive) {
        
        Page<CategoryResponse> responses = categoryService.getAllCategories(page, size, sortBy, sortDir, search, isActive);
        return ResponseEntity.ok(responses);
    }
}
