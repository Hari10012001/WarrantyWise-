package com.warrantywise.dto.servicerecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceMonthlyTrendResponse {
    private int year;
    private int month;
    private String monthName;
    private BigDecimal totalCost;
    private long serviceCount;
}
