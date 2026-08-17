package com.gymmanagement.notification.whatsapp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
public class TwilioWhatsAppMessageSender implements WhatsAppMessageSender {

    private static final String TWILIO_API_URL_TEMPLATE = "https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json";

    private final WhatsAppProperties properties;
    private final RestTemplate restTemplate;

    @Override
    public boolean sendMessage(String toPhoneE164, String message) {
        WhatsAppProperties.Twilio twilio = properties.getTwilio();
        if (twilio.getAccountSid() == null || twilio.getAuthToken() == null || twilio.getFromNumber() == null) {
            log.warn("Twilio WhatsApp credentials are incomplete. Skipping WhatsApp send.");
            return false;
        }

        String url = TWILIO_API_URL_TEMPLATE.formatted(twilio.getAccountSid());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(twilio.getAccountSid(), twilio.getAuthToken());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("From", toWhatsAppAddress(twilio.getFromNumber()));
        formData.add("To", toWhatsAppAddress(toPhoneE164));
        formData.add("Body", message);

        try {
            HttpStatusCode statusCode = restTemplate.postForEntity(url, new HttpEntity<>(formData, headers), String.class)
                    .getStatusCode();
            boolean success = statusCode.is2xxSuccessful();
            if (!success) {
                log.warn("Twilio WhatsApp send failed with status {}", statusCode.value());
            }
            return success;
        } catch (Exception ex) {
            log.error("Twilio WhatsApp send failed", ex);
            return false;
        }
    }

    private String toWhatsAppAddress(String phoneE164) {
        return phoneE164.startsWith("whatsapp:") ? phoneE164 : "whatsapp:" + phoneE164;
    }
}
