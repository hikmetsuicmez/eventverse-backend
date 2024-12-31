package com.hikmetsuicmez.eventverse.controller;

import com.hikmetsuicmez.eventverse.dto.response.ApiResponse;
import com.hikmetsuicmez.eventverse.dto.response.NotificationResponse;
import com.hikmetsuicmez.eventverse.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<List<NotificationResponse>> getUserNotifications() {
        return ApiResponse.success(
            notificationService.getUserNotifications(),
            "Notifications retrieved successfully"
        );
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<NotificationResponse> markAsRead(@PathVariable UUID notificationId) {
        return ApiResponse.success(
            notificationService.markAsRead(notificationId),
            "Notification marked as read successfully"
        );
    }
} 