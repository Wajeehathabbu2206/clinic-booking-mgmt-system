package com.medibook.notification.dto;

import com.medibook.notification.entity.Notification;
import com.medibook.notification.entity.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private Long relatedAppointmentId;
    private boolean read;
    private LocalDateTime createdAt;

    public static NotificationResponse fromEntity(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .relatedAppointmentId(n.getRelatedAppointmentId())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}