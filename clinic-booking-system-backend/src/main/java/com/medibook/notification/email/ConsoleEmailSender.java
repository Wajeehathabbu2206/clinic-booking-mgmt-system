package com.medibook.notification.email;

/** Local/dev/test fallback: logs the email instead of sending it. */
public class ConsoleEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String body) {
        System.out.printf("%n========== MEDIBOOK NOTIFICATION (console mode) ==========%n"
                        + "To: %s%nSubject: %s%n%s%n"
                        + "===========================================================%n",
                to, subject, body);
    }
}
