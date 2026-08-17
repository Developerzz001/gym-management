package com.gymmanagement.notification;

import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.notification.dto.NotificationResponse;
import com.gymmanagement.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "View in-app notifications for the current user")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get paginated notifications for the current user")
    public ApiResponse<PageResponse<NotificationResponse>> getMyNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = userRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow().getId();
        return ApiResponse.success(notificationService.getMyNotifications(userId, page, size));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count for the current user")
    public ApiResponse<Long> getUnreadCount(Authentication authentication) {
        Long userId = userRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow().getId();
        return ApiResponse.success(notificationService.getUnreadCount(userId));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ApiResponse<NotificationResponse> markAsRead(@PathVariable Long id) {
        return ApiResponse.success(notificationService.markAsRead(id));
    }
}
