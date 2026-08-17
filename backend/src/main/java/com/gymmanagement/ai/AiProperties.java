package com.gymmanagement.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    private boolean enabled = false;
    private String provider = "openai";
    private String systemPrompt = "You are an AI assistant for a gym management platform. Give concise, practical, safe answers.";
    private OpenAi openai = new OpenAi();

    @Getter
    @Setter
    public static class OpenAi {
        private String apiKey;
        private String baseUrl = "https://api.openai.com/v1";
        private String model = "gpt-4o-mini";
        private double temperature = 0.3;
        private int maxTokens = 500;
    }
}
