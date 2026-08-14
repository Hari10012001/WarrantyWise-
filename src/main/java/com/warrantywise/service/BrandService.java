package com.warrantywise.service;

import com.warrantywise.dto.brand.BrandRequest;
import com.warrantywise.dto.brand.BrandResponse;
import com.warrantywise.security.UserPrincipal;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BrandService {
    BrandResponse createBrand(BrandRequest request, String ipAddress, UserPrincipal currentUser);
    BrandResponse updateBrand(Long id, BrandRequest request, String ipAddress, UserPrincipal currentUser);
    void deleteBrand(Long id, String ipAddress, UserPrincipal currentUser);
    BrandResponse getBrandById(Long id);
    List<BrandResponse> getAllActiveBrands();
    Page<BrandResponse> getAllBrands(int page, int size, String sortBy, String sortDir, String search, Boolean isActive);
}
