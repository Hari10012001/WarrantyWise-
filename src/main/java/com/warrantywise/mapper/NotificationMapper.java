package com.warrantywise.mapper;

import com.warrantywise.dto.notification.NotificationResponse;
import com.warrantywise.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        NotificationResponse.NotificationResponseBuilder builder = NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .category(notification.getCategory())
                .notificationDate(notification.getNotificationDate())
                .isRead(notification.getIsRead())
                .isDismissed(notification.getIsDismissed())
                .createdAt(notification.getCreatedAt());

        if (notification.getUser() != null) {
            builder.userId(notification.getUser().getId());
        }

        if (notification.getProduct() != null) {
            builder.productId(notification.getProduct().getId());
            builder.productName(notification.getProduct().getName());
        }

        if (notification.getWarranty() != null) {
            builder.warrantyId(notification.getWarranty().getId());
        }

        return builder.build();
    }
}
