package com.medibook.notification.email;


/**
 * Provider-agnostic contract. To add SendGrid/SES later: implement this
 * interface and add one case in EmailConfig.
 */

public interface EmailSender {
    void send(String to, String subject, String body);
}