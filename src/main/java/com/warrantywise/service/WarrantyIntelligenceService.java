package com.warrantywise.service;

import com.warrantywise.dto.warranty.WarrantyHealthResponse;
import com.warrantywise.dto.warranty.WarrantySummaryResponse;
import com.warrantywise.dto.warranty.WarrantyTimelineResponse;
import com.warrantywise.security.UserPrincipal;

import java.util.List;

public interface WarrantyIntelligenceService {
    WarrantySummaryResponse getWarrantySummary(UserPrincipal currentUser);
    WarrantyHealthResponse getProductWarrantyHealth(Long productId, UserPrincipal currentUser);
    List<WarrantyHealthResponse> getUserProductsWarrantyHealth(UserPrincipal currentUser);
    List<WarrantyTimelineResponse> getWarrantyTimeline(UserPrincipal currentUser);
}
