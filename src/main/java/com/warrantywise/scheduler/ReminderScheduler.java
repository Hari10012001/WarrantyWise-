package com.warrantywise.scheduler;

import com.warrantywise.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final ReminderService reminderService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void runDailyWarrantyReminderJob() {
        log.info("Starting daily warranty reminder generation job...");
        try {
            int count = reminderService.generateWarrantyReminders();
            log.info("Completed daily warranty reminder generation job. Created {} reminders.", count);
        } catch (Exception e) {
            log.error("Error running daily warranty reminder generation job", e);
        }
    }
}
