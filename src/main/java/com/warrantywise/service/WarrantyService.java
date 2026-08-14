package com.warrantywise.service;

import com.warrantywise.dto.warranty.WarrantyRequest;
import com.warrantywise.dto.warranty.WarrantyResponse;
import com.warrantywise.security.UserPrincipal;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface WarrantyService {
    WarrantyResponse createWarranty(WarrantyRequest request, String ipAddress, UserPrincipal currentUser);
    
    WarrantyResponse updateWarranty(Long id, WarrantyRequest request, String ipAddress, UserPrincipal currentUser);
    
    void deleteWarranty(Long id, String ipAddress, UserPrincipal currentUser);
    
    WarrantyResponse getWarrantyById(Long id, UserPrincipal currentUser);
    
    List<WarrantyResponse> getWarrantiesByProduct(Long productId, UserPrincipal currentUser);
    
    Page<WarrantyResponse> getActiveWarranties(int page, int size, String sortBy, String sortDir, UserPrincipal currentUser);
    
    Page<WarrantyResponse> getExpiredWarranties(int page, int size, String sortBy, String sortDir, UserPrincipal currentUser);
    
    Page<WarrantyResponse> getExpiringSoonWarranties(int page, int size, String sortBy, String sortDir, UserPrincipal currentUser);
    
    Page<WarrantyResponse> searchWarranties(Long productId, com.warrantywise.enums.WarrantyType warrantyType, com.warrantywise.enums.WarrantyStatus status, String search, LocalDate startDate, LocalDate endDate, Boolean expiringSoon, int page, int size, String sortBy, String sortDir, UserPrincipal currentUser);
}
