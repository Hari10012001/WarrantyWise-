package com.warrantywise.dto.warranty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyHealthResponse {
    private Long productId;
    private String productName;
    private String categoryName;
    private String brandName;
    private double healthScore;
    private String healthStatus;
    private long activeWarranties;
    private long expiringSoonWarranties;
    private long expiredWarranties;
    private List<String> recommendations;
}
