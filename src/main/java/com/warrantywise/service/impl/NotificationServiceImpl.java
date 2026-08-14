package com.warrantywise.service.impl;

import com.warrantywise.dto.notification.NotificationResponse;
import com.warrantywise.entity.Notification;
import com.warrantywise.enums.NotificationCategory;
import com.warrantywise.exception.ResourceNotFoundException;
import com.warrantywise.mapper.NotificationMapper;
import com.warrantywise.repository.NotificationRepository;
import com.warrantywise.security.UserPrincipal;
import com.warrantywise.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(int page, int size, String sortBy, String sortDir, UserPrincipal currentUser) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        return notificationRepository.findByUserIdAndIsDismissedFalse(currentUser.getId(), pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUnreadNotifications(int page, int size, String sortBy, String sortDir, UserPrincipal currentUser) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        return notificationRepository.findByUserIdAndIsReadFalseAndIsDismissedFalse(currentUser.getId(), pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long id, UserPrincipal currentUser) {
        Notification notification = notificationRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id.toString()));
        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsByCategory(NotificationCategory category, int page, int size, String sortBy, String sortDir, UserPrincipal currentUser) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        return notificationRepository.findByUserIdAndCategoryAndIsDismissedFalse(currentUser.getId(), category, pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    public NotificationResponse markAsRead(Long id, UserPrincipal currentUser) {
        Notification notification = notificationRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id.toString()));
        notification.setIsRead(true);
        Notification updatedNotification = notificationRepository.save(notification);
        return notificationMapper.toResponse(updatedNotification);
    }

    @Override
    public NotificationResponse markAsUnread(Long id, UserPrincipal currentUser) {
        Notification notification = notificationRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id.toString()));
        notification.setIsRead(false);
        Notification updatedNotification = notificationRepository.save(notification);
        return notificationMapper.toResponse(updatedNotification);
    }

    @Override
    public void markAllAsRead(UserPrincipal currentUser) {
        notificationRepository.markAllAsReadByUserId(currentUser.getId());
    }

    @Override
    public NotificationResponse dismissNotification(Long id, UserPrincipal currentUser) {
        Notification notification = notificationRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id.toString()));
        notification.setIsDismissed(true);
        Notification updatedNotification = notificationRepository.save(notification);
        return notificationMapper.toResponse(updatedNotification);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UserPrincipal currentUser) {
        return notificationRepository.countByUserIdAndIsReadFalseAndIsDismissedFalse(currentUser.getId());
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }
}
