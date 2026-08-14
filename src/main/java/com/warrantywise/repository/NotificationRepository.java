package com.warrantywise.repository;

import com.warrantywise.entity.Notification;
import com.warrantywise.enums.NotificationCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndIsDismissedFalse(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndIsReadFalseAndIsDismissedFalse(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndCategoryAndIsDismissedFalse(Long userId, NotificationCategory category, Pageable pageable);

    long countByUserIdAndIsReadFalseAndIsDismissedFalse(Long userId);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndIsReadFalse(Long userId);

    Page<Notification> findByUserIdAndCategory(Long userId, NotificationCategory category, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.notificationDate = :date AND n.isDismissed = false")
    List<Notification> findTodaysNotifications(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndWarrantyIdAndCategory(Long userId, Long warrantyId, NotificationCategory category);
}
