package com.warrantywise.controller;

import com.warrantywise.dto.dashboard.DashboardResponse;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getUserDashboard(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(dashboardService.getUserDashboard(currentUser));
    }
}
