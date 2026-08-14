package com.warrantywise.service;

import com.warrantywise.dto.servicerecord.*;
import com.warrantywise.security.UserPrincipal;
import java.util.List;

public interface ServiceAnalyticsService {
    ServiceAnalyticsSummaryResponse getAnalyticsSummary(UserPrincipal currentUser);
    List<ServiceCostByGroupResponse> getCostByProduct(UserPrincipal currentUser);
    List<ServiceCostByGroupResponse> getCostByCategory(UserPrincipal currentUser);
    List<ServiceCostByGroupResponse> getCostByServiceType(UserPrincipal currentUser);
    List<ServiceMonthlyTrendResponse> getMonthlyTrend(UserPrincipal currentUser);
    List<ServiceRecordResponse> getOverdueServices(UserPrincipal currentUser);
    List<ServiceRecordResponse> getUpcomingServices(UserPrincipal currentUser);
    List<ProductServiceSummaryResponse> getMostServicedProducts(UserPrincipal currentUser);
}
