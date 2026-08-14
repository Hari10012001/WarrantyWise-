package com.warrantywise.service;

import com.warrantywise.dto.product.ProductRequest;
import com.warrantywise.dto.product.ProductResponse;
import com.warrantywise.security.UserPrincipal;
import org.springframework.data.domain.Page;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request, String ipAddress, UserPrincipal currentUser);
    ProductResponse updateProduct(Long id, ProductRequest request, String ipAddress, UserPrincipal currentUser);
    void deleteProduct(Long id, String ipAddress, UserPrincipal currentUser);
    ProductResponse getProductById(Long id, UserPrincipal currentUser);
    Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir, UserPrincipal currentUser);
    Page<ProductResponse> getProductsByUser(int page, int size, String sortBy, String sortDir, Boolean isActive, UserPrincipal currentUser);
    Page<ProductResponse> getProductsByCategory(Long categoryId, int page, int size, String sortBy, String sortDir, UserPrincipal currentUser);
    Page<ProductResponse> getProductsByBrand(Long brandId, int page, int size, String sortBy, String sortDir, UserPrincipal currentUser);
    Page<ProductResponse> searchProducts(
            Long categoryId,
            Long brandId,
            com.warrantywise.enums.ProductStatus status,
            com.warrantywise.enums.ProductCondition condition,
            com.warrantywise.enums.PurchaseMode purchaseMode,
            com.warrantywise.enums.WarrantyStatus warrantyStatus,
            Boolean isActive,
            String search,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            Long tagId,
            String storageLocation,
            int page,
            int size,
            String sortBy,
            String sortDir,
            UserPrincipal currentUser);

    com.warrantywise.dto.attachment.AttachmentResponse uploadProductAttachment(Long productId, org.springframework.web.multipart.MultipartFile file, com.warrantywise.enums.AttachmentType attachmentType, String ipAddress, UserPrincipal currentUser);

    com.warrantywise.dto.attachment.AttachmentResponse replaceProductAttachment(Long productId, Long attachmentId, org.springframework.web.multipart.MultipartFile file, com.warrantywise.enums.AttachmentType attachmentType, String ipAddress, UserPrincipal currentUser);

    void deleteProductAttachment(Long productId, Long attachmentId, String ipAddress, UserPrincipal currentUser);

    java.util.List<com.warrantywise.dto.attachment.AttachmentResponse> getProductAttachments(Long productId, UserPrincipal currentUser);
}
