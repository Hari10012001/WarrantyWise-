package com.warrantywise.dto.warranty;

import com.warrantywise.enums.WarrantyStatus;
import com.warrantywise.enums.WarrantyType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Warranty type is required")
    private WarrantyType warrantyType;

    @Size(max = 200, message = "Provider name cannot exceed 200 characters")
    private String provider;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private String coverageDetails;

    private String termsAndConditions;

    @Builder.Default
    private WarrantyStatus status = WarrantyStatus.ACTIVE;
}
