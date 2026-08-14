package com.warrantywise.dto.dashboard;

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
public class DashboardOverviewDto {
    private long totalProducts;
    private long activeProducts;
    private long totalWarranties;
    private long activeWarranties;
    private long expiringSoonWarranties;
    private long expiredWarranties;
    private long totalServiceRecords;
    private BigDecimal totalServiceCost;
    private long unreadNotificationsCount;
    private long todaysRemindersCount;
}
