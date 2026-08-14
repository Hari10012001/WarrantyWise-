package com.warrantywise.dto.report;

import com.warrantywise.enums.ServiceType;
import com.warrantywise.enums.WarrantyStatus;
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
public class ReportFilterRequest {
    private Long productId;
    private Long categoryId;
    private Long brandId;
    private WarrantyStatus warrantyStatus;
    private ServiceType serviceType;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate purchaseStartDate;
    private LocalDate purchaseEndDate;
}
