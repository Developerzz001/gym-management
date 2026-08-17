package com.gymmanagement.notification.whatsapp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.whatsapp")
public class WhatsAppProperties {

    private boolean enabled = false;

    private String provider = "log";

    private String defaultCountryCode = "+91";

    private Twilio twilio = new Twilio();
    private Cloud cloud = new Cloud();

    @Getter
    @Setter
    public static class Twilio {
        private String accountSid;
        private String authToken;
        private String fromNumber;
    }

    @Getter
    @Setter
    public static class Cloud {
        private String apiVersion = "v20.0";
        private String phoneNumberId;
        private String accessToken;
    }
}
