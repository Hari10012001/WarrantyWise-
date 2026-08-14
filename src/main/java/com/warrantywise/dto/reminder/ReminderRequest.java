package com.warrantywise.dto.reminder;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReminderRequest {
    private Long warrantyId;
    private String title;
    private String message;
    private LocalDate reminderDate;
    private Integer daysBeforeExpiry;
    private String channel;
    private LocalDate snoozedUntil;
}
