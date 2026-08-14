package com.warrantywise.service;

import com.warrantywise.dto.notification.NotificationResponse;
import com.warrantywise.enums.NotificationCategory;
import com.warrantywise.security.UserPrincipal;
import org.springframework.data.domain.Page;

public interface NotificationService {
    Page<NotificationResponse> getUserNotifications(int page, int size, String sortBy, String sortDir, UserPrincipal currentUser);
    Page<NotificationResponse> getUnreadNotifications(int page, int size, String sortBy, String sortDir, UserPrincipal currentUser);
    NotificationResponse getNotificationById(Long id, UserPrincipal currentUser);
    Page<NotificationResponse> getNotificationsByCategory(NotificationCategory category, int page, int size, String sortBy, String sortDir, UserPrincipal currentUser);
    NotificationResponse markAsRead(Long id, UserPrincipal currentUser);
    NotificationResponse markAsUnread(Long id, UserPrincipal currentUser);
    void markAllAsRead(UserPrincipal currentUser);
    NotificationResponse dismissNotification(Long id, UserPrincipal currentUser);
    long getUnreadCount(UserPrincipal currentUser);
}
