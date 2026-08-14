package com.warrantywise.dto.notification;

import com.warrantywise.enums.NotificationCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private Long warrantyId;
    private String title;
    private String message;
    private NotificationCategory category;
    private LocalDate notificationDate;
    private Boolean isRead;
    private Boolean isDismissed;
    private LocalDateTime createdAt;
}
