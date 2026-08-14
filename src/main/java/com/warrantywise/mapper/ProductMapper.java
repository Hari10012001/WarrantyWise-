package com.warrantywise.mapper;

import com.warrantywise.dto.auth.UserSummaryResponse;
import com.warrantywise.dto.brand.BrandResponse;
import com.warrantywise.dto.category.CategoryResponse;
import com.warrantywise.dto.product.ProductRequest;
import com.warrantywise.dto.product.ProductResponse;
import com.warrantywise.dto.tag.TagResponse;
import com.warrantywise.entity.Brand;
import com.warrantywise.entity.Category;
import com.warrantywise.entity.Product;
import com.warrantywise.entity.Tag;
import com.warrantywise.entity.User;
import com.warrantywise.enums.ProductCondition;
import com.warrantywise.enums.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final TagMapper tagMapper;

    public Product toEntity(ProductRequest request, User user, Category category, Brand brand, Set<Tag> tags) {
        if (request == null) {
            return null;
        }

        return Product.builder()
                .user(user)
                .category(category)
                .brand(brand)
                .tags(tags != null ? tags : new HashSet<>())
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
    }

    public ProductResponse toResponse(Product product, Long warrantyCount, Long serviceRecordCount, Long attachmentCount) {
        if (product == null) {
            return null;
        }

        UserSummaryResponse userResponse = null;
        if (product.getUser() != null) {
            userResponse = UserSummaryResponse.builder()
                    .id(product.getUser().getId())
                    .fullName(product.getUser().getFullName())
                    .email(product.getUser().getEmail())
                    .role(product.getUser().getRole())
                    .phone(product.getUser().getPhone())
                    .build();
        }

        CategoryResponse categoryResponse = categoryMapper.toResponse(product.getCategory(), null);

        BrandResponse brandResponse = product.getBrand() != null
                ? brandMapper.toResponse(product.getBrand(), null)
                : null;

        Set<TagResponse> tagResponses = null;
        if (product.getTags() != null) {
            tagResponses = product.getTags().stream()
                    .map(tag -> tagMapper.toResponse(tag, null))
                    .collect(Collectors.toSet());
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .modelName(product.getModelName())
                .modelNumber(product.getModelNumber())
                .serialNumber(product.getSerialNumber())
                .color(product.getColor())
                .imeiNumber(product.getImeiNumber())
                .barcode(product.getBarcode())
                .purchaseDate(product.getPurchaseDate())
                .purchasePrice(product.getPurchasePrice())
                .purchaseMode(product.getPurchaseMode())
                .invoiceNumber(product.getInvoiceNumber())
                .retailer(product.getRetailer())
                .productStatus(product.getProductStatus())
                .productCondition(product.getProductCondition())
                .storageLocation(product.getStorageLocation())
                .notes(product.getNotes())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .user(userResponse)
                .category(categoryResponse)
                .brand(brandResponse)
                .tags(tagResponses)
                .warrantyCount(warrantyCount != null ? warrantyCount : 0L)
                .serviceRecordCount(serviceRecordCount != null ? serviceRecordCount : 0L)
                .attachmentCount(attachmentCount != null ? attachmentCount : 0L)
                .build();
    }

    public void updateEntityFromRequest(ProductRequest request, Product product, Category category, Brand brand, Set<Tag> tags) {
        if (request == null || product == null) {
            return;
        }

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getModelName() != null) {
            product.setModelName(request.getModelName());
        }
        if (request.getModelNumber() != null) {
            product.setModelNumber(request.getModelNumber());
        }
        if (request.getSerialNumber() != null) {
            product.setSerialNumber(request.getSerialNumber());
        }
        if (request.getColor() != null) {
            product.setColor(request.getColor());
        }
        if (request.getImeiNumber() != null) {
            product.setImeiNumber(request.getImeiNumber());
        }
        if (request.getBarcode() != null) {
            product.setBarcode(request.getBarcode());
        }
        if (request.getPurchaseDate() != null) {
            product.setPurchaseDate(request.getPurchaseDate());
        }
        if (request.getPurchasePrice() != null) {
            product.setPurchasePrice(request.getPurchasePrice());
        }
        if (request.getPurchaseMode() != null) {
            product.setPurchaseMode(request.getPurchaseMode());
        }
        if (request.getInvoiceNumber() != null) {
            product.setInvoiceNumber(request.getInvoiceNumber());
        }
        if (request.getRetailer() != null) {
            product.setRetailer(request.getRetailer());
        }
        if (request.getProductStatus() != null) {
            product.setProductStatus(request.getProductStatus());
        }
        if (request.getProductCondition() != null) {
            product.setProductCondition(request.getProductCondition());
        }
        if (request.getStorageLocation() != null) {
            product.setStorageLocation(request.getStorageLocation());
        }
        if (request.getNotes() != null) {
            product.setNotes(request.getNotes());
        }
        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }

        if (category != null) {
            product.setCategory(category);
        }
        product.setBrand(brand);
        if (tags != null) {
            product.setTags(tags);
        }
    }
}
