package com.medibook.notification.controller;

import com.medibook.auth.security.CustomUserDetails;
import com.medibook.common.response.ApiResponse;
import com.medibook.notification.dto.NotificationResponse;
import com.medibook.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<NotificationResponse> notifications =
                notificationService.getMyNotifications(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/my/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        long count = notificationService.getUnreadCount(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        NotificationResponse response = notificationService.markAsRead(id, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}