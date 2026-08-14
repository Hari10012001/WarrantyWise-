package com.warrantywise.dto.servicerecord;

import com.warrantywise.enums.ServiceType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRecordResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productModelName;
    private ServiceType serviceType;
    private String serviceProvider;
    private LocalDate serviceDate;
    private LocalDate completionDate;
    private BigDecimal cost;
    private String description;
    private String workPerformed;
    private String partsReplaced;
    private LocalDate nextServiceDate;
    private String serviceStatus;
    private String notes;
    private Long attachmentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
