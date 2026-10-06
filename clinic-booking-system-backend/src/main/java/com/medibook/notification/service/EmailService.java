package com.medibook.notification.service;

import com.medibook.notification.email.EmailSender;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Getter
@Setter
public class EmailService {

    private final EmailSender emailSender;

    /**
     * Fire-and-forget on a dedicated thread pool: a slow or failing email
     * provider never blocks or breaks booking/cancel/reschedule.
     */
    @Async("emailExecutor")
    public void sendEmail(String to, String subject, String body) {
        try {
            emailSender.send(to, subject, body);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}