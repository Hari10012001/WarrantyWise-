package com.warrantywise.dto.servicerecord;

import com.warrantywise.enums.ServiceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRecordRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Service type is required")
    private ServiceType serviceType;

    @Size(max = 200, message = "Service provider name cannot exceed 200 characters")
    private String serviceProvider;

    @NotNull(message = "Service date is required")
    private LocalDate serviceDate;

    private LocalDate completionDate;

    @PositiveOrZero(message = "Cost must be positive or zero")
    private BigDecimal cost;

    private String description;

    private String workPerformed;

    private String partsReplaced;

    private LocalDate nextServiceDate;

    @Builder.Default
    private String serviceStatus = "COMPLETED";

    private String notes;
}
