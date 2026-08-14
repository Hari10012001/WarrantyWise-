package com.warrantywise.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings extends BaseEntity {

    @NotNull(message = "User reference is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @Column(name = "notification_warranty_expiry", nullable = false)
    private Boolean notificationWarrantyExpiry = true;

    @Builder.Default
    @Column(name = "notification_service_due", nullable = false)
    private Boolean notificationServiceDue = true;

    @Builder.Default
    @Min(value = 1, message = "Expiry alert days must be at least 1 day")
    @Max(value = 90, message = "Expiry alert days cannot exceed 90 days")
    @Column(name = "expiry_alert_days", nullable = false)
    private Integer expiryAlertDays = 30;

    @Builder.Default
    @Column(name = "dashboard_layout", length = 20)
    private String dashboardLayout = "grid";

    @Builder.Default
    @Min(value = 5, message = "Items per page must be at least 5")
    @Max(value = 50, message = "Items per page cannot exceed 50")
    @Column(name = "items_per_page", nullable = false)
    private Integer itemsPerPage = 10;
}

