package com.warrantywise.controller;

import com.warrantywise.dto.brand.BrandRequest;
import com.warrantywise.dto.brand.BrandResponse;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.BrandService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandResponse> createBrand(@Valid @RequestBody BrandRequest request,
                                                     @AuthenticationPrincipal UserPrincipal currentUser,
                                                     HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        BrandResponse response = brandService.createBrand(request, ipAddress, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandResponse> updateBrand(@PathVariable Long id,
                                                     @Valid @RequestBody BrandRequest request,
                                                     @AuthenticationPrincipal UserPrincipal currentUser,
                                                     HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        BrandResponse response = brandService.updateBrand(id, request, ipAddress, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id,
                                            @AuthenticationPrincipal UserPrincipal currentUser,
                                            HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        brandService.deleteBrand(id, ipAddress, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getBrandById(@PathVariable Long id) {
        BrandResponse response = brandService.getBrandById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<BrandResponse>> getAllActiveBrands() {
        List<BrandResponse> responses = brandService.getAllActiveBrands();
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    public ResponseEntity<Page<BrandResponse>> getAllBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive) {
        
        Page<BrandResponse> responses = brandService.getAllBrands(page, size, sortBy, sortDir, search, isActive);
        return ResponseEntity.ok(responses);
    }
}
