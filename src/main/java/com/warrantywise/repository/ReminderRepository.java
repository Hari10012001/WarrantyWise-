package com.warrantywise.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.warrantywise.entity.Reminder;
import java.time.LocalDate;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    Page<Reminder> findByUserIdAndIsDismissedFalse(Long userId, Pageable pageable);

    Page<Reminder> findByUserIdAndIsRead(Long userId, Boolean isRead, Pageable pageable);

    Page<Reminder> findByUserIdAndReminderDateAndIsDismissedFalse(Long userId, LocalDate reminderDate, Pageable pageable);

    Page<Reminder> findByUserIdAndReminderDateBeforeAndIsDismissedFalse(Long userId, LocalDate date, Pageable pageable);
    
    // Additional method for upcoming reminders
    Page<Reminder> findByUserIdAndReminderDateAfterAndIsDismissedFalse(Long userId, LocalDate date, Pageable pageable);

    boolean existsByWarrantyIdAndDaysBeforeExpiry(Long warrantyId, Integer daysBeforeExpiry);

    long countByUserIdAndIsReadFalseAndIsDismissedFalse(Long userId);

    @Modifying
    @Query("UPDATE Reminder r SET r.isRead = true WHERE r.user.id = :userId AND r.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);
}
