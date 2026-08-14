package com.warrantywise.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.warrantywise.enums.ActionType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardRecentActivityDto {
    private Long id;
    private ActionType action;
    private String entityType;
    private Long entityId;
    private String description;
    private LocalDateTime timestamp;
}
