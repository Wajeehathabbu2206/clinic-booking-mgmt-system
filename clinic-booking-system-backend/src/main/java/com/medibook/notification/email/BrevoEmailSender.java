package com.medibook.notification.email;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

public class BrevoEmailSender implements EmailSender {

    private final RestClient restClient;
    private final EmailProperties props;

    public BrevoEmailSender(EmailProperties props) {
        this.props = props;
        this.restClient = RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader("api-key", props.getApiKey())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public void send(String to, String subject, String body) {
        Map<String, Object> payload = Map.of(
                "sender", Map.of("name", props.getSenderName(), "email", props.getSenderEmail()),
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "textContent", body
        );

        try {
            restClient.post()
                    .uri("/v3/smtp/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new IllegalStateException(
                    "Brevo rejected the email (" + e.getStatusCode() + "): " + e.getResponseBodyAsString(), e);
        }
    }
}