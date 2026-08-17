package com.gymmanagement.notification;

import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.notification.dto.NotificationResponse;
import com.gymmanagement.user.User;

public interface NotificationService {

    void createNotification(User recipient, NotificationType type, String message);

    PageResponse<NotificationResponse> getMyNotifications(Long userId, int page, int size);

    long getUnreadCount(Long userId);

    NotificationResponse markAsRead(Long id);
}
