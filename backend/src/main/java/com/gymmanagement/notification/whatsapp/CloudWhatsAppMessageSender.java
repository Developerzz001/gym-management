package com.gymmanagement.notification.whatsapp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class CloudWhatsAppMessageSender implements WhatsAppMessageSender {

    private static final String CLOUD_API_URL_TEMPLATE = "https://graph.facebook.com/%s/%s/messages";

    private final WhatsAppProperties properties;
    private final RestTemplate restTemplate;

    @Override
    public boolean sendMessage(String toPhoneE164, String message) {
        WhatsAppProperties.Cloud cloud = properties.getCloud();
        if (cloud.getAccessToken() == null || cloud.getAccessToken().isBlank()
                || cloud.getPhoneNumberId() == null || cloud.getPhoneNumberId().isBlank()) {
            log.warn("WhatsApp Cloud credentials are incomplete. Skipping WhatsApp send.");
            return false;
        }

        String url = CLOUD_API_URL_TEMPLATE.formatted(cloud.getApiVersion(), cloud.getPhoneNumberId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(cloud.getAccessToken());

        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", sanitizePhone(toPhoneE164),
                "type", "text",
                "text", Map.of("body", message)
        );

        try {
            HttpStatusCode statusCode = restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), String.class)
                    .getStatusCode();
            boolean success = statusCode.is2xxSuccessful();
            if (!success) {
                log.warn("WhatsApp Cloud send failed with status {}", statusCode.value());
            }
            return success;
        } catch (Exception ex) {
            log.error("WhatsApp Cloud send failed", ex);
            return false;
        }
    }

    private String sanitizePhone(String toPhoneE164) {
        return toPhoneE164.startsWith("+") ? toPhoneE164.substring(1) : toPhoneE164;
    }
}
