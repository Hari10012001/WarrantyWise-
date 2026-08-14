package com.warrantywise.dto.warranty;

import com.warrantywise.enums.WarrantyStatus;
import com.warrantywise.enums.WarrantyType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productModelName;
    private WarrantyType warrantyType;
    private String provider;
    private LocalDate startDate;
    private LocalDate endDate;
    private String coverageDetails;
    private String termsAndConditions;
    private WarrantyStatus status;
    private Long daysRemaining;
    private Boolean isExpired;
    private Boolean isExpiringSoon;
    private Long attachmentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
