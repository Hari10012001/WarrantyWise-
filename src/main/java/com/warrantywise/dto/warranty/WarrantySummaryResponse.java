package com.warrantywise.dto.warranty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantySummaryResponse {
    private long totalWarranties;
    private long activeCount;
    private long expiringSoonCount;
    private long expiredCount;
    private long extendedCount;
    private long totalProducts;
    private long productsWithActiveWarranty;
    private double warrantyCoveragePercentage;
    private double overallHealthScore;
    private String healthRating;
}
