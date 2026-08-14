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
public class ServiceAnalyticsSummaryResponse {
    private long totalServices;
    private BigDecimal totalServiceCost;
    private BigDecimal averageServiceCost;
    private long servicesThisMonth;
    private BigDecimal costThisMonth;
    private long overdueServicesCount;
    private long upcomingServicesCount;
    private String mostServicedProductName;
}
