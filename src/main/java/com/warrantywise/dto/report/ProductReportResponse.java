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
public class ProductReportResponse {
    private Long productId;
    private String name;
    private String modelName;
    private String categoryName;
    private String brandName;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private String serialNumber;
    private String activeWarrantyStatus;
    private BigDecimal totalServiceCost;
    private long serviceCount;
}
