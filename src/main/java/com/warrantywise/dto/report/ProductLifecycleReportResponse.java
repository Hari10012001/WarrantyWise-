package com.warrantywise.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductLifecycleReportResponse {
    private Long productId;
    private String productName;
    private String categoryName;
    private String brandName;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private long totalWarranties;
    private String activeWarrantyStatus;
    private double warrantyHealthScore;
    private long totalServices;
    private BigDecimal totalServiceCost;
}
