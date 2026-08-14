package com.warrantywise.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardQuickActionDto {
    private String label;
    private String actionUrl;
    private String icon;
    private String type; // "PRIMARY", "SECONDARY", "WARNING", "INFO"
}
