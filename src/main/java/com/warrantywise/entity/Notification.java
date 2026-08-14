package com.warrantywise.entity;

import com.warrantywise.enums.NotificationCategory;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name="idx_notifications_user_read", columnList="user_id, is_read"),
    @Index(name="idx_notifications_user_created", columnList="user_id, created_at"),
    @Index(name="idx_notifications_category", columnList="category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warranty_id")
    private Warranty warranty;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 20, nullable = false)
    private NotificationCategory category;

    @Column(name = "notification_date", nullable = false)
    private LocalDate notificationDate;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Builder.Default
    @Column(name = "is_dismissed", nullable = false)
    private Boolean isDismissed = false;
}
