package com.medibook.notification.email;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "medibook.email")
@Getter
@Setter
public class EmailProperties {

    /** "brevo" or "console" */
    private String provider = "brevo";
    private String apiKey;
    private String senderEmail;
    private String senderName = "MediBook";
    private String baseUrl = "https://api.brevo.com";
}
