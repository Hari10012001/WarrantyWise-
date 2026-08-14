package com.warrantywise.dto.product;

import com.warrantywise.enums.ProductCondition;
import com.warrantywise.enums.ProductStatus;
import com.warrantywise.enums.PurchaseMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name cannot exceed 200 characters")
    private String name;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private Long brandId;

    private Set<Long> tagIds;

    @Size(max = 150)
    private String modelName;

    @Size(max = 100)
    private String modelNumber;

    @Size(max = 100)
    private String serialNumber;

    @Size(max = 50)
    private String color;

    @Size(max = 20)
    private String imeiNumber;

    @Size(max = 100)
    private String barcode;

    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;

    @PositiveOrZero(message = "Purchase price must be positive or zero")
    private BigDecimal purchasePrice;

    private PurchaseMode purchaseMode;

    @Size(max = 100)
    private String invoiceNumber;

    @Size(max = 200)
    private String retailer;

    @Builder.Default
    private ProductStatus productStatus = ProductStatus.IN_USE;

    @Builder.Default
    private ProductCondition productCondition = ProductCondition.NEW;

    @Size(max = 200)
    private String storageLocation;

    private String notes;

    @Builder.Default
    private Boolean isActive = true;
}
