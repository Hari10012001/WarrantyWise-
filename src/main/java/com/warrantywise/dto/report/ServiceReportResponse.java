package com.warrantywise.dto.report;

import com.warrantywise.enums.ServiceType;
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
public class ServiceReportResponse {
    private Long serviceRecordId;
    private Long productId;
    private String productName;
    private ServiceType serviceType;
    private String serviceProvider;
    private LocalDate serviceDate;
    private LocalDate completionDate;
    private BigDecimal cost;
    private String serviceStatus;
}
