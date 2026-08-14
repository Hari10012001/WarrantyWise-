package com.warrantywise.controller;

import com.warrantywise.dto.servicerecord.*;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.ServiceAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/service-records/analytics")
@RequiredArgsConstructor
public class ServiceAnalyticsController {

    private final ServiceAnalyticsService serviceAnalyticsService;

    @GetMapping("/summary")
    public ResponseEntity<ServiceAnalyticsSummaryResponse> getAnalyticsSummary(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(serviceAnalyticsService.getAnalyticsSummary(currentUser));
    }

    @GetMapping("/cost-by-product")
    public ResponseEntity<List<ServiceCostByGroupResponse>> getCostByProduct(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(serviceAnalyticsService.getCostByProduct(currentUser));
    }

    @GetMapping("/cost-by-category")
    public ResponseEntity<List<ServiceCostByGroupResponse>> getCostByCategory(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(serviceAnalyticsService.getCostByCategory(currentUser));
    }

    @GetMapping("/cost-by-service-type")
    public ResponseEntity<List<ServiceCostByGroupResponse>> getCostByServiceType(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(serviceAnalyticsService.getCostByServiceType(currentUser));
    }

    @GetMapping("/monthly-trend")
    public ResponseEntity<List<ServiceMonthlyTrendResponse>> getMonthlyTrend(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(serviceAnalyticsService.getMonthlyTrend(currentUser));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<ServiceRecordResponse>> getOverdueServices(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(serviceAnalyticsService.getOverdueServices(currentUser));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<ServiceRecordResponse>> getUpcomingServices(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(serviceAnalyticsService.getUpcomingServices(currentUser));
    }

    @GetMapping("/most-serviced")
    public ResponseEntity<List<ProductServiceSummaryResponse>> getMostServicedProducts(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(serviceAnalyticsService.getMostServicedProducts(currentUser));
    }
}
