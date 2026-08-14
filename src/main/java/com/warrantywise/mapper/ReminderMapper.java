package com.warrantywise.mapper;

import org.springframework.stereotype.Component;
import com.warrantywise.entity.Reminder;
import com.warrantywise.dto.reminder.ReminderResponse;
import java.time.LocalDate;

@Component
public class ReminderMapper {

    public ReminderResponse toResponse(Reminder reminder) {
        if (reminder == null) {
            return null;
        }

        boolean isSnoozed = reminder.getSnoozedUntil() != null && reminder.getSnoozedUntil().isAfter(LocalDate.now());

        return ReminderResponse.builder()
                .id(reminder.getId())
                .userId(reminder.getUser() != null ? reminder.getUser().getId() : null)
                .productId(reminder.getProduct() != null ? reminder.getProduct().getId() : null)
                .productName(reminder.getProduct() != null ? reminder.getProduct().getName() : null)
                .warrantyId(reminder.getWarranty() != null ? reminder.getWarranty().getId() : null)
                .title(reminder.getTitle())
                .message(reminder.getMessage())
                .reminderDate(reminder.getReminderDate())
                .daysBeforeExpiry(reminder.getDaysBeforeExpiry())
                .channel(reminder.getChannel())
                .category(reminder.getCategory())
                .isRead(reminder.getIsRead())
                .isDismissed(reminder.getIsDismissed())
                .snoozedUntil(reminder.getSnoozedUntil())
                .isSnoozed(isSnoozed)
                .createdAt(reminder.getCreatedAt())
                .build();
    }
}
