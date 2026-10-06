package com.medibook.doctorapplication.event;

import com.medibook.notification.entity.NotificationType;

import java.util.List;

public record DoctorApplicationNotificationEvent(
        List<Long> recipientIds,
        NotificationType type,
        String title,
        String message
) { }
