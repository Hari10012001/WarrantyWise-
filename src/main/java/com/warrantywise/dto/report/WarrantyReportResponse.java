package com.warrantywise.dto.report;

import com.warrantywise.enums.WarrantyStatus;
import com.warrantywise.enums.WarrantyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyReportResponse {
    private Long warrantyId;
    private Long productId;
    private String productName;
    private WarrantyType warrantyType;
    private String provider;
    private LocalDate startDate;
    private LocalDate endDate;
    private WarrantyStatus status;
    private long daysRemaining;
}
