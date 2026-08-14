package com.warrantywise.controller;

import com.warrantywise.dto.product.ProductRequest;
import com.warrantywise.dto.product.ProductResponse;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        
        String ipAddress = httpRequest.getRemoteAddr();
        ProductResponse response = productService.createProduct(request, ipAddress, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
            
        String ipAddress = httpRequest.getRemoteAddr();
        ProductResponse response = productService.updateProduct(id, request, ipAddress, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
            
        String ipAddress = httpRequest.getRemoteAddr();
        productService.deleteProduct(id, ipAddress, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
            
        ProductResponse response = productService.getProductById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser) {
            
        Page<ProductResponse> responses = productService.getAllProducts(page, size, sortBy, sortDir, currentUser);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my-products")
    public ResponseEntity<Page<ProductResponse>> getProductsByUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Boolean isActive,
            @AuthenticationPrincipal UserPrincipal currentUser) {
            
        Page<ProductResponse> responses = productService.getProductsByUser(page, size, sortBy, sortDir, isActive, currentUser);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponse>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser) {
            
        Page<ProductResponse> responses = productService.getProductsByCategory(categoryId, page, size, sortBy, sortDir, currentUser);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/brand/{brandId}")
    public ResponseEntity<Page<ProductResponse>> getProductsByBrand(
            @PathVariable Long brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser) {
            
        Page<ProductResponse> responses = productService.getProductsByBrand(brandId, page, size, sortBy, sortDir, currentUser);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) com.warrantywise.enums.ProductStatus status,
            @RequestParam(required = false) com.warrantywise.enums.ProductCondition condition,
            @RequestParam(required = false) com.warrantywise.enums.PurchaseMode purchaseMode,
            @RequestParam(required = false) com.warrantywise.enums.WarrantyStatus warrantyStatus,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String storageLocation,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Page<ProductResponse> responses = productService.searchProducts(
                categoryId, brandId, status, condition, purchaseMode, warrantyStatus,
                isActive, search, startDate, endDate, tagId, storageLocation,
                page, size, sortBy, sortDir, currentUser);
        return ResponseEntity.ok(responses);
    }

    @PostMapping(value = "/{id}/attachments", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<com.warrantywise.dto.attachment.AttachmentResponse> uploadProductAttachment(
            @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("attachmentType") com.warrantywise.enums.AttachmentType attachmentType,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {

        String ipAddress = httpRequest.getRemoteAddr();
        com.warrantywise.dto.attachment.AttachmentResponse response = productService.uploadProductAttachment(id, file, attachmentType, ipAddress, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}/attachments/{attachmentId}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<com.warrantywise.dto.attachment.AttachmentResponse> replaceProductAttachment(
            @PathVariable Long id,
            @PathVariable Long attachmentId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("attachmentType") com.warrantywise.enums.AttachmentType attachmentType,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {

        String ipAddress = httpRequest.getRemoteAddr();
        com.warrantywise.dto.attachment.AttachmentResponse response = productService.replaceProductAttachment(id, attachmentId, file, attachmentType, ipAddress, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteProductAttachment(
            @PathVariable Long id,
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {

        String ipAddress = httpRequest.getRemoteAddr();
        productService.deleteProductAttachment(id, attachmentId, ipAddress, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/attachments")
    public ResponseEntity<java.util.List<com.warrantywise.dto.attachment.AttachmentResponse>> getProductAttachments(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        java.util.List<com.warrantywise.dto.attachment.AttachmentResponse> responses = productService.getProductAttachments(id, currentUser);
        return ResponseEntity.ok(responses);
    }
}
