package com.warrantywise.service.impl;

import com.warrantywise.dto.servicerecord.*;
import com.warrantywise.entity.ServiceRecord;
import com.warrantywise.mapper.ServiceRecordMapper;
import com.warrantywise.repository.AttachmentRepository;
import com.warrantywise.repository.ServiceRecordRepository;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.ServiceAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceAnalyticsServiceImpl implements ServiceAnalyticsService {

    private final ServiceRecordRepository serviceRecordRepository;
    private final AttachmentRepository attachmentRepository;
    private final ServiceRecordMapper serviceRecordMapper;

    private boolean isAdmin(UserPrincipal currentUser) {
        return currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private Long getUserIdForQuery(UserPrincipal currentUser) {
        return isAdmin(currentUser) ? null : currentUser.getId();
    }

    @Override
    public ServiceAnalyticsSummaryResponse getAnalyticsSummary(UserPrincipal currentUser) {
        Long userId = getUserIdForQuery(currentUser);
        
        long totalServices = userId == null ? serviceRecordRepository.count() : serviceRecordRepository.countByUserId(userId);
        BigDecimal totalServiceCost = serviceRecordRepository.sumTotalCost(userId);
        if(totalServiceCost == null) totalServiceCost = BigDecimal.ZERO;
        
        Double avgCostVal = serviceRecordRepository.avgCost(userId);
        BigDecimal averageServiceCost = avgCostVal != null ? BigDecimal.valueOf(avgCostVal) : BigDecimal.ZERO;

        LocalDate now = LocalDate.now();
        LocalDate firstOfMonth = now.withDayOfMonth(1);
        LocalDate lastOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        long servicesThisMonth = serviceRecordRepository.countServicesBetween(userId, firstOfMonth, lastOfMonth);
        BigDecimal costThisMonth = serviceRecordRepository.sumCostBetween(userId, firstOfMonth, lastOfMonth);
        if(costThisMonth == null) costThisMonth = BigDecimal.ZERO;

        List<ServiceRecord> overdueList = serviceRecordRepository.findOverdueServicesByUser(userId);
        List<ServiceRecord> upcomingList = serviceRecordRepository.findUpcomingServicesByUser(userId);

        List<Object[]> productCostList = serviceRecordRepository.findServiceCostByProduct(userId);
        String mostServicedProductName = productCostList.isEmpty() ? null : (String) productCostList.get(0)[0];

        return ServiceAnalyticsSummaryResponse.builder()
                .totalServices(totalServices)
                .totalServiceCost(totalServiceCost)
                .averageServiceCost(averageServiceCost)
                .servicesThisMonth(servicesThisMonth)
                .costThisMonth(costThisMonth)
                .overdueServicesCount(overdueList != null ? overdueList.size() : 0)
                .upcomingServicesCount(upcomingList != null ? upcomingList.size() : 0)
                .mostServicedProductName(mostServicedProductName)
                .build();
    }

    @Override
    public List<ServiceCostByGroupResponse> getCostByProduct(UserPrincipal currentUser) {
        Long userId = getUserIdForQuery(currentUser);
        List<Object[]> results = serviceRecordRepository.findServiceCostByProduct(userId);
        return mapToServiceCostByGroupResponse(results);
    }

    @Override
    public List<ServiceCostByGroupResponse> getCostByCategory(UserPrincipal currentUser) {
        Long userId = getUserIdForQuery(currentUser);
        List<Object[]> results = serviceRecordRepository.findServiceCostByCategory(userId);
        return mapToServiceCostByGroupResponse(results);
    }

    @Override
    public List<ServiceCostByGroupResponse> getCostByServiceType(UserPrincipal currentUser) {
        Long userId = getUserIdForQuery(currentUser);
        List<Object[]> results = serviceRecordRepository.findServiceCostByServiceType(userId);
        return mapToServiceCostByGroupResponse(results);
    }

    private List<ServiceCostByGroupResponse> mapToServiceCostByGroupResponse(List<Object[]> results) {
        return results.stream().map(result -> ServiceCostByGroupResponse.builder()
                .groupName(result[0] != null ? result[0].toString() : "Unknown")
                .totalCost((BigDecimal) result[1])
                .serviceCount(((Number) result[2]).longValue())
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<ServiceMonthlyTrendResponse> getMonthlyTrend(UserPrincipal currentUser) {
        Long userId = getUserIdForQuery(currentUser);
        List<Object[]> results = serviceRecordRepository.findMonthlyTrend(userId);
        return results.stream().map(result -> {
            int year = ((Number) result[0]).intValue();
            int month = ((Number) result[1]).intValue();
            return ServiceMonthlyTrendResponse.builder()
                    .year(year)
                    .month(month)
                    .monthName(Month.of(month).name())
                    .totalCost((BigDecimal) result[2])
                    .serviceCount(((Number) result[3]).longValue())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<ServiceRecordResponse> getOverdueServices(UserPrincipal currentUser) {
        Long userId = getUserIdForQuery(currentUser);
        List<ServiceRecord> overdueList = serviceRecordRepository.findOverdueServicesByUser(userId);
        return overdueList.stream().map(r -> {
            long count = attachmentRepository.countByEntityTypeAndEntityId("SERVICE_RECORD", r.getId());
            return serviceRecordMapper.toResponse(r, count);
        }).collect(Collectors.toList());
    }

    @Override
    public List<ServiceRecordResponse> getUpcomingServices(UserPrincipal currentUser) {
        Long userId = getUserIdForQuery(currentUser);
        List<ServiceRecord> upcomingList = serviceRecordRepository.findUpcomingServicesByUser(userId);
        return upcomingList.stream().map(r -> {
            long count = attachmentRepository.countByEntityTypeAndEntityId("SERVICE_RECORD", r.getId());
            return serviceRecordMapper.toResponse(r, count);
        }).collect(Collectors.toList());
    }

    @Override
    public List<ProductServiceSummaryResponse> getMostServicedProducts(UserPrincipal currentUser) {
        Long userId = getUserIdForQuery(currentUser);
        List<Object[]> productCostList = serviceRecordRepository.findServiceCostByProduct(userId);

        return productCostList.stream().map(result -> {
            Long productId = result.length > 3 && result[3] != null ? ((Number) result[3]).longValue() : null;
            LocalDate lastServiceDate = result.length > 4 ? (LocalDate) result[4] : null;
            LocalDate nextServiceDate = result.length > 5 ? (LocalDate) result[5] : null;
            return ProductServiceSummaryResponse.builder()
                    .productName((String) result[0])
                    .totalCost((BigDecimal) result[1])
                    .serviceCount(((Number) result[2]).longValue())
                    .productId(productId)
                    .lastServiceDate(lastServiceDate)
                    .nextServiceDate(nextServiceDate)
                    .build();
        }).collect(Collectors.toList());
    }
}
