package com.warrantywise.dto.servicerecord;

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
public class ProductServiceSummaryResponse {
    private Long productId;
    private String productName;
    private long serviceCount;
    private BigDecimal totalCost;
    private LocalDate lastServiceDate;
    private LocalDate nextServiceDate;
}
