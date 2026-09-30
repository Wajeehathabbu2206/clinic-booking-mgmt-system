package com.medibook.notification.email;

import lombok.extern.slf4j.Slf4j;

/** Local/dev/test fallback: logs the email instead of sending it. */
@Slf4j
public class ConsoleEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String body) {
        log.info("[EMAIL - console mode] to={} | subject={} | body={}", to, subject, body);
    }
}