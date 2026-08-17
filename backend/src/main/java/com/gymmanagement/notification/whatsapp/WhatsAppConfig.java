package com.gymmanagement.notification.whatsapp;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import static org.springframework.util.StringUtils.hasText;

@Configuration
@EnableConfigurationProperties(WhatsAppProperties.class)
public class WhatsAppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public WhatsAppMessageSender whatsAppMessageSender(WhatsAppProperties properties, RestTemplate restTemplate) {
        if (!properties.isEnabled()) {
            return new LogWhatsAppMessageSender();
        }

        if ("cloud".equalsIgnoreCase(properties.getProvider())
                && hasText(properties.getCloud().getAccessToken())
                && hasText(properties.getCloud().getPhoneNumberId())) {
            return new CloudWhatsAppMessageSender(properties, restTemplate);
        }

        if (hasText(properties.getTwilio().getAccountSid())
                && hasText(properties.getTwilio().getAuthToken())
                && hasText(properties.getTwilio().getFromNumber())) {
            return new TwilioWhatsAppMessageSender(properties, restTemplate);
        }

        return new LogWhatsAppMessageSender();
    }
}
