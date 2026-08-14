package com.warrantywise.dto.product;

import com.warrantywise.dto.auth.UserSummaryResponse;
import com.warrantywise.dto.brand.BrandResponse;
import com.warrantywise.dto.category.CategoryResponse;
import com.warrantywise.dto.tag.TagResponse;
import com.warrantywise.enums.ProductCondition;
import com.warrantywise.enums.ProductStatus;
import com.warrantywise.enums.PurchaseMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String modelName;
    private String modelNumber;
    private String serialNumber;
    private String color;
    private String imeiNumber;
    private String barcode;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private PurchaseMode purchaseMode;
    private String invoiceNumber;
    private String retailer;
    private ProductStatus productStatus;
    private ProductCondition productCondition;
    private String storageLocation;
    private String notes;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserSummaryResponse user;
    private CategoryResponse category;
    private BrandResponse brand;
    private Set<TagResponse> tags;

    private Long warrantyCount;
    private Long serviceRecordCount;
    private Long attachmentCount;
}
