package com.warrantywise.service;

import org.springframework.data.domain.Page;
import com.warrantywise.dto.reminder.ReminderResponse;
import com.warrantywise.security.UserPrincipal;

public interface ReminderService {
    int generateWarrantyReminders();
    ReminderResponse markAsRead(Long id, UserPrincipal currentUser);
    ReminderResponse markAsUnread(Long id, UserPrincipal currentUser);
    void markAllAsRead(UserPrincipal currentUser);
    ReminderResponse dismissReminder(Long id, UserPrincipal currentUser);
    ReminderResponse snoozeReminder(Long id, int snoozeDays, UserPrincipal currentUser);
    Page<ReminderResponse> getUpcomingReminders(int page, int size, UserPrincipal currentUser);
    Page<ReminderResponse> getTodaysReminders(int page, int size, UserPrincipal currentUser);
    Page<ReminderResponse> getExpiredReminders(int page, int size, UserPrincipal currentUser);
    long getUnreadCount(UserPrincipal currentUser);
}
