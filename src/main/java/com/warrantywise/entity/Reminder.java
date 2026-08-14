package com.warrantywise.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import com.warrantywise.enums.NotificationCategory;

@Entity
@Table(name = "reminders", indexes = {
    @Index(name="idx_reminders_user_read", columnList="user_id, is_read"),
    @Index(name="idx_reminders_user_date", columnList="user_id, reminder_date"),
    @Index(name="idx_reminders_warranty_days", columnList="warranty_id, days_before_expiry")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reminder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warranty_id")
    private Warranty warranty;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "reminder_date", nullable = false)
    private LocalDate reminderDate;

    @Column(name = "days_before_expiry")
    private Integer daysBeforeExpiry;

    @Column(length = 50)
    @Builder.Default
    private String channel = "DASHBOARD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationCategory category;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "is_dismissed", nullable = false)
    @Builder.Default
    private Boolean isDismissed = false;

    @Column(name = "snoozed_until")
    private LocalDate snoozedUntil;
}
