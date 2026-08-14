package com.warrantywise.controller;

import com.warrantywise.dto.report.*;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/products")
    public ResponseEntity<List<ProductReportResponse>> getProductReport(
            ReportFilterRequest filter,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(reportService.getProductReport(filter, currentUser));
    }

    @GetMapping("/products/export-csv")
    public ResponseEntity<String> exportProductReportCsv(
            ReportFilterRequest filter,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        String csv = reportService.exportProductReportCsv(filter, currentUser);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/warranties")
    public ResponseEntity<List<WarrantyReportResponse>> getWarrantyReport(
            ReportFilterRequest filter,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(reportService.getWarrantyReport(filter, currentUser));
    }

    @GetMapping("/warranties/export-csv")
    public ResponseEntity<String> exportWarrantyReportCsv(
            ReportFilterRequest filter,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        String csv = reportService.exportWarrantyReportCsv(filter, currentUser);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=warranties_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServiceReportResponse>> getServiceReport(
            ReportFilterRequest filter,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(reportService.getServiceReport(filter, currentUser));
    }

    @GetMapping("/services/export-csv")
    public ResponseEntity<String> exportServiceReportCsv(
            ReportFilterRequest filter,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        String csv = reportService.exportServiceReportCsv(filter, currentUser);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=services_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/lifecycle")
    public ResponseEntity<List<ProductLifecycleReportResponse>> getProductLifecycleReport(
            ReportFilterRequest filter,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(reportService.getProductLifecycleReport(filter, currentUser));
    }

    @GetMapping("/lifecycle/export-csv")
    public ResponseEntity<String> exportProductLifecycleReportCsv(
            ReportFilterRequest filter,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        String csv = reportService.exportProductLifecycleReportCsv(filter, currentUser);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=lifecycle_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
