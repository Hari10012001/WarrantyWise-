package com.warrantywise.service.impl;

import com.warrantywise.dto.product.ProductRequest;
import com.warrantywise.dto.product.ProductResponse;
import com.warrantywise.entity.*;
import com.warrantywise.enums.*;
import com.warrantywise.exception.*;
import com.warrantywise.mapper.ProductMapper;
import com.warrantywise.repository.*;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final TagRepository tagRepository;
    private final WarrantyRepository warrantyRepository;
    private final ServiceRecordRepository serviceRecordRepository;
    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ProductMapper productMapper;
    private final com.warrantywise.service.AttachmentService attachmentService;

    @Override
    public ProductResponse createProduct(ProductRequest request, String ipAddress, UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        if (request.getSerialNumber() != null && !request.getSerialNumber().isBlank() && 
            productRepository.existsBySerialNumberAndUserId(request.getSerialNumber(), currentUser.getId())) {
            throw new DuplicateResourceException("Product with serial number '" + request.getSerialNumber() + "' already exists for this user");
        }

        if (request.getImeiNumber() != null && !request.getImeiNumber().isBlank() && 
            productRepository.existsByImeiNumberAndUserId(request.getImeiNumber(), currentUser.getId())) {
            throw new DuplicateResourceException("Product with IMEI number '" + request.getImeiNumber() + "' already exists for this user");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseGet(() -> categoryRepository.findByIsActiveTrueOrderByNameAsc().stream().findFirst()
                .orElseGet(() -> categoryRepository.save(Category.builder().name("General").description("General Products Category").isActive(true).build())));

        Brand brand = request.getBrandId() != null ? 
                brandRepository.findById(request.getBrandId()).orElse(null) : null;

        Set<Tag> tags = (request.getTagIds() != null && !request.getTagIds().isEmpty()) ? 
                new HashSet<>(tagRepository.findAllById(request.getTagIds())) : new HashSet<>();

        Product product = Product.builder()
                .user(user)
                .category(category)
                .brand(brand)
                .tags(tags)
                .name(request.getName())
                .modelName(request.getModelName())
                .modelNumber(request.getModelNumber())
                .serialNumber(request.getSerialNumber())
                .color(request.getColor())
                .imeiNumber(request.getImeiNumber())
                .barcode(request.getBarcode())
                .purchaseDate(request.getPurchaseDate())
                .purchasePrice(request.getPurchasePrice())
                .purchaseMode(request.getPurchaseMode())
                .invoiceNumber(request.getInvoiceNumber())
                .retailer(request.getRetailer())
                .productStatus(request.getProductStatus() != null ? request.getProductStatus() : ProductStatus.IN_USE)
                .productCondition(request.getProductCondition() != null ? request.getProductCondition() : ProductCondition.NEW)
                .storageLocation(request.getStorageLocation())
                .notes(request.getNotes())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Product saved = productRepository.save(product);

        ActivityLog log = ActivityLog.builder()
                .action(ActionType.CREATE)
                .entityType("PRODUCT")
                .entityId(saved.getId())
                .description("Created product: " + saved.getName())
                .user(user)
                .ipAddress(ipAddress)
                .build();
        activityLogRepository.save(log);

        return productMapper.toResponse(saved, 0L, 0L, 0L);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request, String ipAddress, UserPrincipal currentUser) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !product.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Access denied");
        }

        if (request.getSerialNumber() != null && !request.getSerialNumber().isBlank() && 
            !request.getSerialNumber().equals(product.getSerialNumber()) &&
            productRepository.existsBySerialNumberAndUserId(request.getSerialNumber(), product.getUser().getId())) {
            throw new DuplicateResourceException("Product with serial number '" + request.getSerialNumber() + "' already exists for this user");
        }

        if (request.getImeiNumber() != null && !request.getImeiNumber().isBlank() && 
            !request.getImeiNumber().equals(product.getImeiNumber()) &&
            productRepository.existsByImeiNumberAndUserId(request.getImeiNumber(), product.getUser().getId())) {
            throw new DuplicateResourceException("Product with IMEI number '" + request.getImeiNumber() + "' already exists for this user");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseGet(() -> categoryRepository.findByIsActiveTrueOrderByNameAsc().stream().findFirst()
                .orElseGet(() -> categoryRepository.save(Category.builder().name("General").description("General Products Category").isActive(true).build())));

        Brand brand = request.getBrandId() != null ? 
                brandRepository.findById(request.getBrandId()).orElse(null) : null;

        Set<Tag> tags = (request.getTagIds() != null && !request.getTagIds().isEmpty()) ? 
                new HashSet<>(tagRepository.findAllById(request.getTagIds())) : new HashSet<>();

        product.setCategory(category);
        product.setBrand(brand);
        product.setTags(tags);
        product.setName(request.getName());
        product.setModelName(request.getModelName());
        product.setModelNumber(request.getModelNumber());
        product.setSerialNumber(request.getSerialNumber());
        product.setColor(request.getColor());
        product.setImeiNumber(request.getImeiNumber());
        product.setBarcode(request.getBarcode());
        product.setPurchaseDate(request.getPurchaseDate());
        product.setPurchasePrice(request.getPurchasePrice());
        product.setPurchaseMode(request.getPurchaseMode());
        product.setInvoiceNumber(request.getInvoiceNumber());
        product.setRetailer(request.getRetailer());
        product.setProductStatus(request.getProductStatus() != null ? request.getProductStatus() : product.getProductStatus());
        product.setProductCondition(request.getProductCondition() != null ? request.getProductCondition() : product.getProductCondition());
        product.setStorageLocation(request.getStorageLocation());
        product.setNotes(request.getNotes());
        product.setIsActive(request.getIsActive() != null ? request.getIsActive() : product.getIsActive());

        Product updated = productRepository.save(product);

        long warrantyCount = warrantyRepository.countByProductId(id);
        long serviceRecordCount = serviceRecordRepository.countByProductId(id);
        long attachmentCount = attachmentRepository.countByEntityTypeAndEntityId("PRODUCT", id);

        ActivityLog log = ActivityLog.builder()
                .action(ActionType.UPDATE)
                .entityType("PRODUCT")
                .entityId(id)
                .description("Updated product: " + updated.getName())
                .user(updated.getUser())
                .ipAddress(ipAddress)
                .build();
        activityLogRepository.save(log);

        return productMapper.toResponse(updated, warrantyCount, serviceRecordCount, attachmentCount);
    }

    @Override
    public void deleteProduct(Long id, String ipAddress, UserPrincipal currentUser) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !product.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Access denied");
        }

        product.setIsActive(false);
        productRepository.save(product);

        ActivityLog log = ActivityLog.builder()
                .action(ActionType.DELETE)
                .entityType("PRODUCT")
                .entityId(id)
                .description("Deleted product: " + product.getName())
                .user(product.getUser())
                .ipAddress(ipAddress)
                .build();
        activityLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id, UserPrincipal currentUser) {
        Product product = productRepository.findByIdAndUserIdWithDetails(id, currentUser.getId())
                .orElseGet(() -> productRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id)));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !product.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Access denied");
        }

        long warrantyCount = warrantyRepository.countByProductId(id);
        long serviceRecordCount = serviceRecordRepository.countByProductId(id);
        long attachmentCount = attachmentRepository.countByEntityTypeAndEntityId("PRODUCT", id);

        return productMapper.toResponse(product, warrantyCount, serviceRecordCount, attachmentCount);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir, UserPrincipal currentUser) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Page<Product> products;
        if (isAdmin) {
            products = productRepository.findAll(pageable);
        } else {
            products = productRepository.findByUserId(currentUser.getId(), pageable);
        }

        return products.map(p -> productMapper.toResponse(p, 
                warrantyRepository.countByProductId(p.getId()),
                serviceRecordRepository.countByProductId(p.getId()),
                attachmentRepository.countByEntityTypeAndEntityId("PRODUCT", p.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByUser(int page, int size, String sortBy, String sortDir, Boolean isActive, UserPrincipal currentUser) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products;
        if (isActive != null) {
            products = productRepository.findByUserIdAndIsActive(currentUser.getId(), isActive, pageable);
        } else {
            products = productRepository.findByUserId(currentUser.getId(), pageable);
        }

        return products.map(p -> productMapper.toResponse(p, 
                warrantyRepository.countByProductId(p.getId()),
                serviceRecordRepository.countByProductId(p.getId()),
                attachmentRepository.countByEntityTypeAndEntityId("PRODUCT", p.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(Long categoryId, int page, int size, String sortBy, String sortDir, UserPrincipal currentUser) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products = productRepository.findByUserIdAndCategoryIdAndIsActive(currentUser.getId(), categoryId, true, pageable);

        return products.map(p -> productMapper.toResponse(p, 
                warrantyRepository.countByProductId(p.getId()),
                serviceRecordRepository.countByProductId(p.getId()),
                attachmentRepository.countByEntityTypeAndEntityId("PRODUCT", p.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByBrand(Long brandId, int page, int size, String sortBy, String sortDir, UserPrincipal currentUser) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products = productRepository.findByUserIdAndBrandIdAndIsActive(currentUser.getId(), brandId, true, pageable);

        return products.map(p -> productMapper.toResponse(p, 
                warrantyRepository.countByProductId(p.getId()),
                serviceRecordRepository.countByProductId(p.getId()),
                attachmentRepository.countByEntityTypeAndEntityId("PRODUCT", p.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(
            Long categoryId,
            Long brandId,
            ProductStatus status,
            ProductCondition condition,
            PurchaseMode purchaseMode,
            WarrantyStatus warrantyStatus,
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
            UserPrincipal currentUser) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Long targetUserId = isAdmin ? null : currentUser.getId();

        org.springframework.data.jpa.domain.Specification<Product> spec = com.warrantywise.specification.ProductSpecification.filter(
                targetUserId,
                categoryId,
                brandId,
                status,
                condition,
                purchaseMode,
                warrantyStatus,
                isActive,
                search,
                startDate,
                endDate,
                tagId,
                storageLocation
        );

        Page<Product> products = productRepository.findAll(spec, pageable);

        return products.map(p -> productMapper.toResponse(p, 
                warrantyRepository.countByProductId(p.getId()),
                serviceRecordRepository.countByProductId(p.getId()),
                attachmentRepository.countByEntityTypeAndEntityId("PRODUCT", p.getId())));
    }

    @Override
    public com.warrantywise.dto.attachment.AttachmentResponse uploadProductAttachment(Long productId, org.springframework.web.multipart.MultipartFile file, com.warrantywise.enums.AttachmentType attachmentType, String ipAddress, UserPrincipal currentUser) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !product.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Access denied: You can only upload attachments for your own products");
        }

        return attachmentService.uploadAttachment(file, "PRODUCT", productId, attachmentType, ipAddress, currentUser);
    }

    @Override
    public com.warrantywise.dto.attachment.AttachmentResponse replaceProductAttachment(Long productId, Long attachmentId, org.springframework.web.multipart.MultipartFile file, com.warrantywise.enums.AttachmentType attachmentType, String ipAddress, UserPrincipal currentUser) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !product.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Access denied");
        }

        attachmentService.deleteAttachment(attachmentId, ipAddress, currentUser);
        return attachmentService.uploadAttachment(file, "PRODUCT", productId, attachmentType, ipAddress, currentUser);
    }

    @Override
    public void deleteProductAttachment(Long productId, Long attachmentId, String ipAddress, UserPrincipal currentUser) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !product.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Access denied");
        }

        attachmentService.deleteAttachment(attachmentId, ipAddress, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<com.warrantywise.dto.attachment.AttachmentResponse> getProductAttachments(Long productId, UserPrincipal currentUser) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !product.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Access denied");
        }

        return attachmentService.getAttachmentsForEntity("PRODUCT", productId, currentUser);
    }
}
