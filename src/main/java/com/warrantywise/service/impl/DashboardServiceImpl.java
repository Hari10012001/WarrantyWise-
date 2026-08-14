package com.warrantywise.service.impl;

import com.warrantywise.dto.dashboard.DashboardOverviewDto;
import com.warrantywise.dto.dashboard.DashboardQuickActionDto;
import com.warrantywise.dto.dashboard.DashboardRecentActivityDto;
import com.warrantywise.dto.dashboard.DashboardResponse;
import com.warrantywise.dto.product.ProductResponse;
import com.warrantywise.repository.ActivityLogRepository;
import com.warrantywise.repository.AttachmentRepository;
import com.warrantywise.repository.ProductRepository;
import com.warrantywise.repository.ServiceRecordRepository;
import com.warrantywise.repository.WarrantyRepository;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.DashboardService;
import com.warrantywise.service.NotificationService;
import com.warrantywise.service.ReminderService;
import com.warrantywise.service.ServiceAnalyticsService;
import com.warrantywise.service.WarrantyIntelligenceService;
import com.warrantywise.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final ProductRepository productRepository;
    private final WarrantyRepository warrantyRepository;
    private final ServiceRecordRepository serviceRecordRepository;
    private final AttachmentRepository attachmentRepository;
    private final WarrantyIntelligenceService warrantyIntelligenceService;
    private final ServiceAnalyticsService serviceAnalyticsService;
    private final ReminderService reminderService;
    private final NotificationService notificationService;
    private final ActivityLogRepository activityLogRepository;
    private final ProductMapper productMapper;

    @Override
    public DashboardResponse getUserDashboard(UserPrincipal currentUser) {
        var warrantySummary = warrantyIntelligenceService.getWarrantySummary(currentUser);
        var warrantyTimeline = warrantyIntelligenceService.getWarrantyTimeline(currentUser);
        var serviceSummary = serviceAnalyticsService.getAnalyticsSummary(currentUser);
        var todaysReminders = reminderService.getTodaysReminders(0, 5, currentUser).getContent();
        var upcomingReminders = reminderService.getUpcomingReminders(0, 5, currentUser).getContent();
        var recentNotifications = notificationService.getUserNotifications(0, 5, "createdAt", "DESC", currentUser).getContent();
        var unreadCount = notificationService.getUnreadCount(currentUser);
        
        var recentProducts = productRepository.findTop3ByUserIdAndIsActiveTrueOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(p -> {
                    long wCount = warrantyRepository.countByProductId(p.getId());
                    long sCount = serviceRecordRepository.countByProductId(p.getId());
                    long aCount = attachmentRepository.countByEntityTypeAndEntityId("PRODUCT", p.getId());
                    return productMapper.toResponse(p, wCount, sCount, aCount);
                })
                .collect(Collectors.toList());

        var recentActivities = activityLogRepository.findTop5ByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(log -> DashboardRecentActivityDto.builder()
                        .id(log.getId())
                        .action(log.getAction())
                        .entityType(log.getEntityType())
                        .entityId(log.getEntityId())
                        .description(log.getDescription())
                        .timestamp(log.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        var overview = DashboardOverviewDto.builder()
                .totalProducts(warrantySummary.getTotalProducts())
                .activeProducts(warrantySummary.getProductsWithActiveWarranty())
                .totalWarranties(warrantySummary.getTotalWarranties())
                .activeWarranties(warrantySummary.getActiveCount())
                .expiringSoonWarranties(warrantySummary.getExpiringSoonCount())
                .expiredWarranties(warrantySummary.getExpiredCount())
                .totalServiceRecords(serviceSummary.getTotalServices())
                .totalServiceCost(serviceSummary.getTotalServiceCost())
                .unreadNotificationsCount(unreadCount)
                .todaysRemindersCount((long) todaysReminders.size())
                .build();

        List<DashboardQuickActionDto> quickActions = Arrays.asList(
                new DashboardQuickActionDto("Add Product", "/products/new", "bi-plus-lg", "PRIMARY"),
                new DashboardQuickActionDto("Add Warranty", "/warranties/new", "bi-shield-plus", "PRIMARY"),
                new DashboardQuickActionDto("Add Service Record", "/service-records/new", "bi-tools", "SECONDARY"),
                new DashboardQuickActionDto("View Expiring Warranties", "/warranties/expiring", "bi-exclamation-triangle", "WARNING")
        );

        return DashboardResponse.builder()
                .overview(overview)
                .warrantySummary(warrantySummary)
                .warrantyTimeline(warrantyTimeline)
                .recentProducts(recentProducts)
                .serviceSummary(serviceSummary)
                .todaysReminders(todaysReminders)
                .upcomingReminders(upcomingReminders)
                .recentNotifications(recentNotifications)
                .recentActivities(recentActivities)
                .quickActions(quickActions)
                .build();
    }
}
