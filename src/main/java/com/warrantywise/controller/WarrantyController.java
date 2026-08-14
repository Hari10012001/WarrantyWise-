package com.warrantywise.controller;

import com.warrantywise.dto.warranty.WarrantyRequest;
import com.warrantywise.dto.warranty.WarrantyResponse;
import com.warrantywise.enums.WarrantyStatus;
import com.warrantywise.enums.WarrantyType;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.WarrantyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/warranties")
@RequiredArgsConstructor
public class WarrantyController {

    private final WarrantyService warrantyService;

    @PostMapping
    public ResponseEntity<WarrantyResponse> createWarranty(
            @Valid @RequestBody WarrantyRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        WarrantyResponse response = warrantyService.createWarranty(request, ipAddress, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WarrantyResponse> updateWarranty(
            @PathVariable Long id,
            @Valid @RequestBody WarrantyRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        WarrantyResponse response = warrantyService.updateWarranty(id, request, ipAddress, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarranty(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        warrantyService.deleteWarranty(id, ipAddress, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarrantyResponse> getWarrantyById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        WarrantyResponse response = warrantyService.getWarrantyById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<WarrantyResponse>> getWarrantiesByProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<WarrantyResponse> response = warrantyService.getWarrantiesByProduct(productId, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<Page<WarrantyResponse>> getActiveWarranties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Page<WarrantyResponse> response = warrantyService.getActiveWarranties(page, size, sortBy, sortDir, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expired")
    public ResponseEntity<Page<WarrantyResponse>> getExpiredWarranties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Page<WarrantyResponse> response = warrantyService.getExpiredWarranties(page, size, sortBy, sortDir, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<Page<WarrantyResponse>> getExpiringSoonWarranties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Page<WarrantyResponse> response = warrantyService.getExpiringSoonWarranties(page, size, sortBy, sortDir, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<WarrantyResponse>> searchWarranties(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) WarrantyType warrantyType,
            @RequestParam(required = false) WarrantyStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Boolean expiringSoon,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Page<WarrantyResponse> response = warrantyService.searchWarranties(
                productId, warrantyType, status, search, startDate, endDate, expiringSoon, page, size, sortBy, sortDir, currentUser);
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
