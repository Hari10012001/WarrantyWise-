package com.warrantywise.dto.dashboard;

import com.warrantywise.dto.product.ProductResponse;
import com.warrantywise.dto.reminder.ReminderResponse;
import com.warrantywise.dto.notification.NotificationResponse;
import com.warrantywise.dto.warranty.WarrantySummaryResponse;
import com.warrantywise.dto.warranty.WarrantyTimelineResponse;
import com.warrantywise.dto.servicerecord.ServiceAnalyticsSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {
    private DashboardOverviewDto overview;
    private WarrantySummaryResponse warrantySummary;
    private List<WarrantyTimelineResponse> warrantyTimeline;
    private List<ProductResponse> recentProducts;
    private ServiceAnalyticsSummaryResponse serviceSummary;
    private List<ReminderResponse> todaysReminders;
    private List<ReminderResponse> upcomingReminders;
    private List<NotificationResponse> recentNotifications;
    private List<DashboardRecentActivityDto> recentActivities;
    private List<DashboardQuickActionDto> quickActions;
}
