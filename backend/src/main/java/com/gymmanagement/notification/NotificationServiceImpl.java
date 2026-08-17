package com.gymmanagement.notification;

import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.notification.dto.NotificationResponse;
import com.gymmanagement.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void createNotification(User recipient, NotificationType type, String message) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .message(message)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    public PageResponse<NotificationResponse> getMyNotifications(Long userId, int page, int size) {
        Page<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(
                userId, PageRequest.of(page, size, Sort.by("id").descending()));
        return PageResponse.from(notifications.map(notificationMapper::toResponse));
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        notification.setRead(true);
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }
}
