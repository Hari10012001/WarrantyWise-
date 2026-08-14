package com.warrantywise.dto.reminder;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.warrantywise.enums.NotificationCategory;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReminderResponse {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private Long warrantyId;
    private String title;
    private String message;
    private LocalDate reminderDate;
    private Integer daysBeforeExpiry;
    private String channel;
    private NotificationCategory category;
    private Boolean isRead;
    private Boolean isDismissed;
    private LocalDate snoozedUntil;
    private Boolean isSnoozed;
    private LocalDateTime createdAt;
}
