package com.warrantywise.service.impl;

import com.warrantywise.dto.report.*;
import com.warrantywise.dto.warranty.WarrantyHealthResponse;
import com.warrantywise.entity.Product;
import com.warrantywise.entity.ServiceRecord;
import com.warrantywise.entity.Warranty;
import com.warrantywise.enums.WarrantyStatus;
import com.warrantywise.repository.ProductRepository;
import com.warrantywise.repository.ServiceRecordRepository;
import com.warrantywise.repository.WarrantyRepository;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.ReportService;
import com.warrantywise.service.WarrantyIntelligenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final ProductRepository productRepository;
    private final WarrantyRepository warrantyRepository;
    private final ServiceRecordRepository serviceRecordRepository;
    private final WarrantyIntelligenceService warrantyIntelligenceService;

    private Long getUserId(UserPrincipal currentUser) {
        if (currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return null; // Admin can see all
        }
        return currentUser.getId();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String stringValue = value.replace("\"", "\"\"");
        if (stringValue.contains(",") || stringValue.contains("\"") || stringValue.contains("\n")) {
            return "\"" + stringValue + "\"";
        }
        return stringValue;
    }

    @Override
    public List<ProductReportResponse> getProductReport(ReportFilterRequest filter, UserPrincipal currentUser) {
        Long userId = getUserId(currentUser);
        List<Product> products;
        if (userId != null) {
            products = productRepository.findByUserIdAndIsActiveTrue(userId);
        } else {
            products = productRepository.findAll();
        }

        return products.stream()
                .filter(p -> filter.getCategoryId() == null || (p.getCategory() != null && p.getCategory().getId().equals(filter.getCategoryId())))
                .filter(p -> filter.getBrandId() == null || (p.getBrand() != null && p.getBrand().getId().equals(filter.getBrandId())))
                .filter(p -> filter.getPurchaseStartDate() == null || (p.getPurchaseDate() != null && !p.getPurchaseDate().isBefore(filter.getPurchaseStartDate())))
                .filter(p -> filter.getPurchaseEndDate() == null || (p.getPurchaseDate() != null && !p.getPurchaseDate().isAfter(filter.getPurchaseEndDate())))
                .map(p -> {
                    List<ServiceRecord> services = serviceRecordRepository.findByProductId(p.getId());
                    BigDecimal totalCost = services.stream()
                            .map(ServiceRecord::getCost)
                            .filter(c -> c != null)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    List<Warranty> warranties = warrantyRepository.findByProductId(p.getId());
                    boolean hasActive = warranties.stream().anyMatch(w -> w.getStatus() == WarrantyStatus.ACTIVE);
                    boolean hasExpired = warranties.stream().anyMatch(w -> w.getStatus() == WarrantyStatus.EXPIRED);
                    String status = hasActive ? "ACTIVE" : (hasExpired ? "EXPIRED" : "NONE");

                    return ProductReportResponse.builder()
                            .productId(p.getId())
                            .name(p.getName())
                            .modelName(p.getModelNumber())
                            .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                            .brandName(p.getBrand() != null ? p.getBrand().getName() : null)
                            .purchaseDate(p.getPurchaseDate())
                            .purchasePrice(p.getPurchasePrice())
                            .serialNumber(p.getSerialNumber())
                            .activeWarrantyStatus(status)
                            .totalServiceCost(totalCost)
                            .serviceCount(services.size())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public String exportProductReportCsv(ReportFilterRequest filter, UserPrincipal currentUser) {
        List<ProductReportResponse> data = getProductReport(filter, currentUser);
        StringBuilder csv = new StringBuilder();
        csv.append("Product ID,Name,Model,Category,Brand,Purchase Date,Purchase Price,Serial Number,Warranty Status,Total Service Cost,Service Count\n");
        for (ProductReportResponse item : data) {
            csv.append(item.getProductId()).append(",")
               .append(escapeCsv(item.getName())).append(",")
               .append(escapeCsv(item.getModelName())).append(",")
               .append(escapeCsv(item.getCategoryName())).append(",")
               .append(escapeCsv(item.getBrandName())).append(",")
               .append(item.getPurchaseDate() != null ? item.getPurchaseDate() : "").append(",")
               .append(item.getPurchasePrice() != null ? item.getPurchasePrice() : "").append(",")
               .append(escapeCsv(item.getSerialNumber())).append(",")
               .append(escapeCsv(item.getActiveWarrantyStatus())).append(",")
               .append(item.getTotalServiceCost() != null ? item.getTotalServiceCost() : "0").append(",")
               .append(item.getServiceCount()).append("\n");
        }
        return csv.toString();
    }

    @Override
    public List<WarrantyReportResponse> getWarrantyReport(ReportFilterRequest filter, UserPrincipal currentUser) {
        Long userId = getUserId(currentUser);
        List<Warranty> warranties;

        if (userId != null) {
             List<Product> products = productRepository.findByUserIdAndIsActiveTrue(userId);
             warranties = products.stream()
                .flatMap(p -> warrantyRepository.findByProductId(p.getId()).stream())
                .collect(Collectors.toList());
        } else {
            warranties = warrantyRepository.findAll();
        }

        return warranties.stream()
                .filter(w -> filter.getProductId() == null || w.getProduct().getId().equals(filter.getProductId()))
                .filter(w -> filter.getWarrantyStatus() == null || w.getStatus() == filter.getWarrantyStatus())
                .filter(w -> filter.getStartDate() == null || (w.getStartDate() != null && !w.getStartDate().isBefore(filter.getStartDate())))
                .filter(w -> filter.getEndDate() == null || (w.getEndDate() != null && !w.getEndDate().isAfter(filter.getEndDate())))
                .map(w -> {
                    long daysRemaining = 0;
                    if (w.getEndDate() != null && (w.getStatus() == WarrantyStatus.ACTIVE || w.getEndDate().isAfter(LocalDate.now()))) {
                        daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), w.getEndDate());
                        if(daysRemaining < 0) daysRemaining = 0;
                    }
                    return WarrantyReportResponse.builder()
                            .warrantyId(w.getId())
                            .productId(w.getProduct().getId())
                            .productName(w.getProduct().getName())
                            .warrantyType(w.getWarrantyType())
                            .provider(w.getProvider())
                            .startDate(w.getStartDate())
                            .endDate(w.getEndDate())
                            .status(w.getStatus())
                            .daysRemaining(daysRemaining)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public String exportWarrantyReportCsv(ReportFilterRequest filter, UserPrincipal currentUser) {
        List<WarrantyReportResponse> data = getWarrantyReport(filter, currentUser);
        StringBuilder csv = new StringBuilder();
        csv.append("Warranty ID,Product ID,Product Name,Warranty Type,Provider,Start Date,End Date,Status,Days Remaining\n");
        for (WarrantyReportResponse item : data) {
            csv.append(item.getWarrantyId()).append(",")
               .append(item.getProductId()).append(",")
               .append(escapeCsv(item.getProductName())).append(",")
               .append(item.getWarrantyType() != null ? item.getWarrantyType().name() : "").append(",")
               .append(escapeCsv(item.getProvider())).append(",")
               .append(item.getStartDate() != null ? item.getStartDate() : "").append(",")
               .append(item.getEndDate() != null ? item.getEndDate() : "").append(",")
               .append(item.getStatus() != null ? item.getStatus().name() : "").append(",")
               .append(item.getDaysRemaining()).append("\n");
        }
        return csv.toString();
    }

    @Override
    public List<ServiceReportResponse> getServiceReport(ReportFilterRequest filter, UserPrincipal currentUser) {
        Long userId = getUserId(currentUser);
        List<ServiceRecord> services;

        if (userId != null) {
            List<Product> products = productRepository.findByUserIdAndIsActiveTrue(userId);
            services = products.stream()
                .flatMap(p -> serviceRecordRepository.findByProductId(p.getId()).stream())
                .collect(Collectors.toList());
        } else {
            services = serviceRecordRepository.findAll();
        }

        return services.stream()
                .filter(s -> filter.getProductId() == null || s.getProduct().getId().equals(filter.getProductId()))
                .filter(s -> filter.getServiceType() == null || s.getServiceType() == filter.getServiceType())
                .filter(s -> filter.getStartDate() == null || (s.getServiceDate() != null && !s.getServiceDate().isBefore(filter.getStartDate())))
                .filter(s -> filter.getEndDate() == null || (s.getServiceDate() != null && !s.getServiceDate().isAfter(filter.getEndDate())))
                .map(s -> ServiceReportResponse.builder()
                        .serviceRecordId(s.getId())
                        .productId(s.getProduct().getId())
                        .productName(s.getProduct().getName())
                        .serviceType(s.getServiceType())
                        .serviceProvider(s.getServiceProvider())
                        .serviceDate(s.getServiceDate())
                        .completionDate(s.getCompletionDate())
                        .cost(s.getCost())
                        .serviceStatus(s.getServiceStatus())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public String exportServiceReportCsv(ReportFilterRequest filter, UserPrincipal currentUser) {
        List<ServiceReportResponse> data = getServiceReport(filter, currentUser);
        StringBuilder csv = new StringBuilder();
        csv.append("Service Record ID,Product ID,Product Name,Service Type,Service Provider,Service Date,Completion Date,Cost,Status\n");
        for (ServiceReportResponse item : data) {
            csv.append(item.getServiceRecordId()).append(",")
               .append(item.getProductId()).append(",")
               .append(escapeCsv(item.getProductName())).append(",")
               .append(item.getServiceType() != null ? item.getServiceType().name() : "").append(",")
               .append(escapeCsv(item.getServiceProvider())).append(",")
               .append(item.getServiceDate() != null ? item.getServiceDate() : "").append(",")
               .append(item.getCompletionDate() != null ? item.getCompletionDate() : "").append(",")
               .append(item.getCost() != null ? item.getCost() : "0").append(",")
               .append(escapeCsv(item.getServiceStatus())).append("\n");
        }
        return csv.toString();
    }

    @Override
    public List<ProductLifecycleReportResponse> getProductLifecycleReport(ReportFilterRequest filter, UserPrincipal currentUser) {
        List<ProductReportResponse> baseProducts = getProductReport(filter, currentUser);

        return baseProducts.stream().map(p -> {
            List<Warranty> warranties = warrantyRepository.findByProductId(p.getProductId());
            double healthScore = 0.0;
            try {
                WarrantyHealthResponse healthResponse = warrantyIntelligenceService.getProductWarrantyHealth(p.getProductId(), currentUser);
                if (healthResponse != null) {
                    healthScore = healthResponse.getHealthScore();
                }
            } catch (Exception e) {
                // Ignore if any exception during health score calculation
            }

            return ProductLifecycleReportResponse.builder()
                    .productId(p.getProductId())
                    .productName(p.getName())
                    .categoryName(p.getCategoryName())
                    .brandName(p.getBrandName())
                    .purchaseDate(p.getPurchaseDate())
                    .purchasePrice(p.getPurchasePrice())
                    .totalWarranties(warranties.size())
                    .activeWarrantyStatus(p.getActiveWarrantyStatus())
                    .warrantyHealthScore(healthScore)
                    .totalServices(p.getServiceCount())
                    .totalServiceCost(p.getTotalServiceCost())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public String exportProductLifecycleReportCsv(ReportFilterRequest filter, UserPrincipal currentUser) {
        List<ProductLifecycleReportResponse> data = getProductLifecycleReport(filter, currentUser);
        StringBuilder csv = new StringBuilder();
        csv.append("Product ID,Product Name,Category,Brand,Purchase Date,Purchase Price,Total Warranties,Active Warranty Status,Warranty Health Score,Total Services,Total Service Cost\n");
        for (ProductLifecycleReportResponse item : data) {
            csv.append(item.getProductId()).append(",")
               .append(escapeCsv(item.getProductName())).append(",")
               .append(escapeCsv(item.getCategoryName())).append(",")
               .append(escapeCsv(item.getBrandName())).append(",")
               .append(item.getPurchaseDate() != null ? item.getPurchaseDate() : "").append(",")
               .append(item.getPurchasePrice() != null ? item.getPurchasePrice() : "").append(",")
               .append(item.getTotalWarranties()).append(",")
               .append(escapeCsv(item.getActiveWarrantyStatus())).append(",")
               .append(item.getWarrantyHealthScore()).append(",")
               .append(item.getTotalServices()).append(",")
               .append(item.getTotalServiceCost() != null ? item.getTotalServiceCost() : "0").append("\n");
        }
        return csv.toString();
    }
}
