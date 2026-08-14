package com.warrantywise.controller;

import com.warrantywise.dto.servicerecord.ServiceRecordRequest;
import com.warrantywise.dto.servicerecord.ServiceRecordResponse;
import com.warrantywise.enums.ServiceType;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.ServiceRecordService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/service-records")
public class ServiceRecordController {

    private final ServiceRecordService serviceRecordService;

    public ServiceRecordController(ServiceRecordService serviceRecordService) {
        this.serviceRecordService = serviceRecordService;
    }

    @PostMapping
    public ResponseEntity<ServiceRecordResponse> createServiceRecord(
            @Valid @RequestBody ServiceRecordRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        ServiceRecordResponse response = serviceRecordService.createServiceRecord(request, httpRequest.getRemoteAddr(), currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceRecordResponse> updateServiceRecord(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRecordRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        ServiceRecordResponse response = serviceRecordService.updateServiceRecord(id, request, httpRequest.getRemoteAddr(), currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServiceRecord(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        serviceRecordService.deleteServiceRecord(id, httpRequest.getRemoteAddr(), currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceRecordResponse> getServiceRecordById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ServiceRecordResponse response = serviceRecordService.getServiceRecordById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ServiceRecordResponse>> getServiceRecordsByProduct(
            @PathVariable Long productId,
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "10", required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = "serviceDate", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Page<ServiceRecordResponse> responses = serviceRecordService.getServiceRecordsByProduct(productId, page, size, sortBy, sortDir, currentUser);
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    public ResponseEntity<Page<ServiceRecordResponse>> getUserServiceRecords(
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "10", required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = "serviceDate", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Page<ServiceRecordResponse> responses = serviceRecordService.getUserServiceRecords(page, size, sortBy, sortDir, currentUser);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ServiceRecordResponse>> searchServiceRecords(
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "serviceType", required = false) ServiceType serviceType,
            @RequestParam(value = "serviceProvider", required = false) String serviceProvider,
            @RequestParam(value = "serviceStatus", required = false) String serviceStatus,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate,
            @RequestParam(value = "upcomingOnly", required = false) Boolean upcomingOnly,
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "10", required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = "serviceDate", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Page<ServiceRecordResponse> responses = serviceRecordService.searchServiceRecords(productId, serviceType, serviceProvider, serviceStatus, search, startDate, endDate, upcomingOnly, page, size, sortBy, sortDir, currentUser);
        return ResponseEntity.ok(responses);
    }
}
