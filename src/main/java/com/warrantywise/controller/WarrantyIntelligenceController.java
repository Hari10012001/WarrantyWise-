package com.warrantywise.controller;

import com.warrantywise.dto.warranty.WarrantyHealthResponse;
import com.warrantywise.dto.warranty.WarrantySummaryResponse;
import com.warrantywise.dto.warranty.WarrantyTimelineResponse;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.WarrantyIntelligenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warranties/intelligence")
@RequiredArgsConstructor
public class WarrantyIntelligenceController {

    private final WarrantyIntelligenceService warrantyIntelligenceService;

    @GetMapping("/summary")
    public ResponseEntity<WarrantySummaryResponse> getWarrantySummary(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(warrantyIntelligenceService.getWarrantySummary(currentUser));
    }

    @GetMapping("/health/{productId}")
    public ResponseEntity<WarrantyHealthResponse> getProductWarrantyHealth(@PathVariable Long productId, @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(warrantyIntelligenceService.getProductWarrantyHealth(productId, currentUser));
    }

    @GetMapping("/health")
    public ResponseEntity<List<WarrantyHealthResponse>> getUserProductsWarrantyHealth(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(warrantyIntelligenceService.getUserProductsWarrantyHealth(currentUser));
    }

    @GetMapping("/timeline")
    public ResponseEntity<List<WarrantyTimelineResponse>> getWarrantyTimeline(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(warrantyIntelligenceService.getWarrantyTimeline(currentUser));
    }
}
