package com.warrantywise.controller;

import com.warrantywise.dto.reminder.ReminderResponse;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping("/generate")
    public ResponseEntity<String> generateWarrantyReminders() {
        int count = reminderService.generateWarrantyReminders();
        return ResponseEntity.ok("Generated " + count + " reminders.");
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ReminderResponse> markAsRead(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(reminderService.markAsRead(id, currentUser));
    }

    @PutMapping("/{id}/unread")
    public ResponseEntity<ReminderResponse> markAsUnread(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(reminderService.markAsUnread(id, currentUser));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserPrincipal currentUser) {
        reminderService.markAllAsRead(currentUser);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/dismiss")
    public ResponseEntity<ReminderResponse> dismissReminder(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(reminderService.dismissReminder(id, currentUser));
    }

    @PutMapping("/{id}/snooze")
    public ResponseEntity<ReminderResponse> snoozeReminder(
            @PathVariable Long id, 
            @RequestParam(defaultValue = "7") int days, 
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(reminderService.snoozeReminder(id, days, currentUser));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Page<ReminderResponse>> getUpcomingReminders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(reminderService.getUpcomingReminders(page, size, currentUser));
    }

    @GetMapping("/today")
    public ResponseEntity<Page<ReminderResponse>> getTodaysReminders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(reminderService.getTodaysReminders(page, size, currentUser));
    }

    @GetMapping("/expired")
    public ResponseEntity<Page<ReminderResponse>> getExpiredReminders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(reminderService.getExpiredReminders(page, size, currentUser));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(reminderService.getUnreadCount(currentUser));
    }
}
