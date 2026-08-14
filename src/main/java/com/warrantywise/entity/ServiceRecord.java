package com.warrantywise.entity;

import com.warrantywise.enums.ServiceType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "service_records", indexes = {
    @Index(name="idx_services_product", columnList="product_id"),
    @Index(name="idx_services_next_date", columnList="next_service_date"),
    @Index(name="idx_services_service_date", columnList="service_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", length = 20, nullable = false)
    private ServiceType serviceType;

    @Column(name = "service_provider", length = 200)
    private String serviceProvider;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cost", precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "next_service_date")
    private LocalDate nextServiceDate;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Column(name = "work_performed", columnDefinition = "TEXT")
    private String workPerformed;

    @Column(name = "parts_replaced", columnDefinition = "TEXT")
    private String partsReplaced;

    @Column(name = "service_status", length = 50)
    @Builder.Default
    private String serviceStatus = "COMPLETED";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
