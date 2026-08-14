package com.warrantywise.entity;

import com.warrantywise.enums.ProductCondition;
import com.warrantywise.enums.ProductStatus;
import com.warrantywise.enums.PurchaseMode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "products", indexes = {
    @Index(name="idx_products_user_active", columnList="user_id, is_active"),
    @Index(name="idx_products_category", columnList="category_id"),
    @Index(name="idx_products_brand", columnList="brand_id"),
    @Index(name="idx_products_purchase_date", columnList="purchase_date"),
    @Index(name="idx_products_status", columnList="product_status"),
    @Index(name="idx_products_user_created", columnList="user_id, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Category is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Builder.Default
    @ManyToMany
    @JoinTable(
        name = "product_tags",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Warranty> warranties = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceRecord> serviceRecords = new ArrayList<>();

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name cannot exceed 200 characters")
    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Size(max = 150, message = "Model name cannot exceed 150 characters")
    @Column(name = "model_name", length = 150)
    private String modelName;

    @Size(max = 100, message = "Model number cannot exceed 100 characters")
    @Column(name = "model_number", length = 100)
    private String modelNumber;

    @Size(max = 100, message = "Serial number cannot exceed 100 characters")
    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Size(max = 50, message = "Color cannot exceed 50 characters")
    @Column(name = "color", length = 50)
    private String color;

    @Size(max = 20, message = "IMEI number cannot exceed 20 characters")
    @Column(name = "imei_number", length = 20)
    private String imeiNumber;

    @Size(max = 100, message = "Barcode cannot exceed 100 characters")
    @Column(name = "barcode", length = 100)
    private String barcode;

    @NotNull(message = "Purchase date is required")
    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @PositiveOrZero(message = "Purchase price must be positive or zero")
    @Column(name = "purchase_price", precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_mode", length = 20)
    private PurchaseMode purchaseMode;

    @Size(max = 100, message = "Invoice number cannot exceed 100 characters")
    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Size(max = 200, message = "Retailer cannot exceed 200 characters")
    @Column(name = "retailer", length = 200)
    private String retailer;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "product_status", length = 20)
    private ProductStatus productStatus = ProductStatus.IN_USE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "product_condition", length = 20)
    private ProductCondition productCondition = ProductCondition.NEW;

    @Size(max = 200, message = "Storage location cannot exceed 200 characters")
    @Column(name = "storage_location", length = 200)
    private String storageLocation;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}

