package com.warrantywise.service;

import com.warrantywise.dto.servicerecord.ServiceRecordRequest;
import com.warrantywise.dto.servicerecord.ServiceRecordResponse;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.enums.ServiceType;
import org.springframework.data.domain.Page;
import java.time.LocalDate;
import java.util.List;

public interface ServiceRecordService {
    ServiceRecordResponse createServiceRecord(ServiceRecordRequest request, String ipAddress, UserPrincipal currentUser);
    ServiceRecordResponse updateServiceRecord(Long id, ServiceRecordRequest request, String ipAddress, UserPrincipal currentUser);
    void deleteServiceRecord(Long id, String ipAddress, UserPrincipal currentUser);
    ServiceRecordResponse getServiceRecordById(Long id, UserPrincipal currentUser);
    Page<ServiceRecordResponse> getServiceRecordsByProduct(Long productId, int page, int size, String sortBy, String sortDir, UserPrincipal currentUser);
    Page<ServiceRecordResponse> getUserServiceRecords(int page, int size, String sortBy, String sortDir, UserPrincipal currentUser);
    Page<ServiceRecordResponse> searchServiceRecords(Long productId, ServiceType serviceType, String serviceProvider, String serviceStatus, String search, LocalDate startDate, LocalDate endDate, Boolean upcomingOnly, int page, int size, String sortBy, String sortDir, UserPrincipal currentUser);
}
