package com.medibook.notification.entity;

import com.medibook.common.entity.BaseEntity;
import com.medibook.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "related_appointment_id")
    private Long relatedAppointmentId;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    // Older databases may still have the original `read` column after the
    // field was renamed to `is_read`. Keep it populated until that legacy
    // column is removed by a database migration.
    @Column(name = "`read`", nullable = false)
    private boolean legacyRead;

    @PrePersist
    @PreUpdate
    private void syncLegacyReadColumn() {
        legacyRead = read;
    }
}
