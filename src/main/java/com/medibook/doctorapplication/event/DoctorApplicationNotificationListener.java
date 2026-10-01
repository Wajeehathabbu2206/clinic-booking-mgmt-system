package com.medibook.doctorapplication.event;

import com.medibook.notification.service.NotificationService;
import com.medibook.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class DoctorApplicationNotificationListener {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationNotification(DoctorApplicationNotificationEvent event) {
        for (Long recipientId : event.recipientIds()) {
            try {
                userRepository.findById(recipientId).ifPresent(user ->
                        notificationService.notifyDoctorApplication(
                                user, event.type(), event.title(), event.message()));
            } catch (RuntimeException ex) {
                log.warn("Could not send doctor application notification to user {}", recipientId, ex);
            }
        }
    }
}
