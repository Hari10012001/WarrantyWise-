package com.warrantywise.service;

import com.warrantywise.dto.dashboard.DashboardResponse;
import com.warrantywise.security.UserPrincipal;

public interface DashboardService {
    DashboardResponse getUserDashboard(UserPrincipal currentUser);
}
