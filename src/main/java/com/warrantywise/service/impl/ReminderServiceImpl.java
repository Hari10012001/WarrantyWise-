package com.warrantywise.service.impl;

import com.warrantywise.dto.reminder.ReminderResponse;
import com.warrantywise.entity.Reminder;
import com.warrantywise.entity.Warranty;
import com.warrantywise.enums.NotificationCategory;
import com.warrantywise.mapper.ReminderMapper;
import com.warrantywise.repository.ReminderRepository;
import com.warrantywise.repository.WarrantyRepository;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReminderServiceImpl implements ReminderService {

    private final ReminderRepository reminderRepository;
    private final WarrantyRepository warrantyRepository;
    private final ReminderMapper reminderMapper;

    @Override
    public int generateWarrantyReminders() {
        int[] intervals = {90, 30, 15, 7, 3, 1, 0};
        int createdCount = 0;
        
        for (int interval : intervals) {
            List<Warranty> warranties;
            if (interval > 0) {
                LocalDate targetDate = LocalDate.now().plusDays(interval);
                warranties = warrantyRepository.findExpiringBetween(targetDate, targetDate);
            } else {
                LocalDate today = LocalDate.now();
                warranties = warrantyRepository.findExpiringBetween(today, today);
                warranties.addAll(warrantyRepository.findNewlyExpired());
            }

            for (Warranty warranty : warranties) {
                if (!reminderRepository.existsByWarrantyIdAndDaysBeforeExpiry(warranty.getId(), interval)) {
                    String title = interval > 0 ? "Warranty Expiry Alert (" + interval + " Days Remaining)" : "Warranty Has Expired";
                    String message = "Your warranty for " + warranty.getProduct().getName() + " with provider " + warranty.getProvider() + " is expiring on " + warranty.getEndDate() + ".";
                    
                    Reminder reminder = Reminder.builder()
                        .title(title)
                        .message(message)
                        .user(warranty.getProduct().getUser())
                        .product(warranty.getProduct())
                        .warranty(warranty)
                        .reminderDate(LocalDate.now())
                        .daysBeforeExpiry(interval)
                        .channel("DASHBOARD")
                        .category(NotificationCategory.WARRANTY_EXPIRY)
                        .isRead(false)
                        .isDismissed(false)
                        .build();
                        
                    reminderRepository.save(reminder);
                    createdCount++;
                }
            }
        }
        return createdCount;
    }

    private Reminder getReminderIfAuthorized(Long id, UserPrincipal currentUser) {
        Reminder reminder = reminderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Reminder not found"));
            
        boolean isAdmin = currentUser.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
        if (!reminder.getUser().getId().equals(currentUser.getId()) && !isAdmin) {
            throw new RuntimeException("Access denied");
        }
        return reminder;
    }

    @Override
    public ReminderResponse markAsRead(Long id, UserPrincipal currentUser) {
        Reminder reminder = getReminderIfAuthorized(id, currentUser);
        reminder.setIsRead(true);
        return reminderMapper.toResponse(reminderRepository.save(reminder));
    }

    @Override
    public ReminderResponse markAsUnread(Long id, UserPrincipal currentUser) {
        Reminder reminder = getReminderIfAuthorized(id, currentUser);
        reminder.setIsRead(false);
        return reminderMapper.toResponse(reminderRepository.save(reminder));
    }

    @Override
    public void markAllAsRead(UserPrincipal currentUser) {
        reminderRepository.markAllAsReadByUserId(currentUser.getId());
    }

    @Override
    public ReminderResponse dismissReminder(Long id, UserPrincipal currentUser) {
        Reminder reminder = getReminderIfAuthorized(id, currentUser);
        reminder.setIsDismissed(true);
        return reminderMapper.toResponse(reminderRepository.save(reminder));
    }

    @Override
    public ReminderResponse snoozeReminder(Long id, int snoozeDays, UserPrincipal currentUser) {
        Reminder reminder = getReminderIfAuthorized(id, currentUser);
        reminder.setSnoozedUntil(LocalDate.now().plusDays(snoozeDays));
        return reminderMapper.toResponse(reminderRepository.save(reminder));
    }

    private Page<ReminderResponse> filterSnoozed(Page<Reminder> page, Pageable pageable) {
        LocalDate today = LocalDate.now();
        List<ReminderResponse> filteredList = page.getContent().stream()
            .filter(r -> r.getSnoozedUntil() == null || !r.getSnoozedUntil().isAfter(today))
            .map(reminderMapper::toResponse)
            .collect(Collectors.toList());
            
        return new PageImpl<>(filteredList, pageable, page.getTotalElements());
    }

    @Override
    public Page<ReminderResponse> getUpcomingReminders(int page, int size, UserPrincipal currentUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("reminderDate").ascending());
        Page<Reminder> reminders = reminderRepository.findByUserIdAndReminderDateAfterAndIsDismissedFalse(currentUser.getId(), LocalDate.now(), pageable);
        return filterSnoozed(reminders, pageable);
    }

    @Override
    public Page<ReminderResponse> getTodaysReminders(int page, int size, UserPrincipal currentUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Reminder> reminders = reminderRepository.findByUserIdAndReminderDateAndIsDismissedFalse(currentUser.getId(), LocalDate.now(), pageable);
        return filterSnoozed(reminders, pageable);
    }

    @Override
    public Page<ReminderResponse> getExpiredReminders(int page, int size, UserPrincipal currentUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("reminderDate").descending());
        Page<Reminder> reminders = reminderRepository.findByUserIdAndReminderDateBeforeAndIsDismissedFalse(currentUser.getId(), LocalDate.now(), pageable);
        return filterSnoozed(reminders, pageable);
    }

    @Override
    public long getUnreadCount(UserPrincipal currentUser) {
        return reminderRepository.countByUserIdAndIsReadFalseAndIsDismissedFalse(currentUser.getId());
    }
}
