package com.medibook.notification.email;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties(EmailProperties.class)
public class EmailConfig {

    @Bean
    public EmailSender emailSender(EmailProperties props) {
        if ("brevo".equalsIgnoreCase(props.getProvider())) {
            // Fail fast at startup instead of silently failing on the first booking
            if (!StringUtils.hasText(props.getApiKey()) || !StringUtils.hasText(props.getSenderEmail())) {
                throw new IllegalStateException(
                        "EMAIL_PROVIDER=brevo requires BREVO_API_KEY and EMAIL_SENDER_ADDRESS to be set");
            }
            return new BrevoEmailSender(props);
        }
        if ("console".equalsIgnoreCase(props.getProvider())) {
            return new ConsoleEmailSender();
        }
        throw new IllegalStateException("Unsupported EMAIL_PROVIDER: " + props.getProvider()
                + ". Supported values are 'brevo' and 'console'.");
    }
}
