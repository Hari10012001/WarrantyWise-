package com.warrantywise.entity;

import com.warrantywise.enums.WarrantyStatus;
import com.warrantywise.enums.WarrantyType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "warranties", indexes = {
    @Index(name="idx_warranties_product", columnList="product_id"),
    @Index(name="idx_warranties_end_date", columnList="end_date"),
    @Index(name="idx_warranties_status", columnList="status"),
    @Index(name="idx_warranties_status_end", columnList="status, end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warranty extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "warranty_type", length = 20, nullable = false)
    private WarrantyType warrantyType;

    @Column(name = "provider", length = 200)
    private String provider;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "coverage_details", columnDefinition = "TEXT")
    private String coverageDetails;

    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private WarrantyStatus status = WarrantyStatus.ACTIVE;
}
