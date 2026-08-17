package com.gymmanagement.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmanagement.ai.dto.AskAiResponse;
import com.gymmanagement.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AskAiServiceImpl implements AskAiService {

    private final AiProperties aiProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public AskAiResponse ask(String question) {
        if (!aiProperties.isEnabled()) {
            throw new BadRequestException("AI assistant is disabled. Enable app.ai.enabled to use Ask AI.");
        }

        if (!"openai".equalsIgnoreCase(aiProperties.getProvider())) {
            throw new BadRequestException("Unsupported AI provider configured. Use provider=openai.");
        }

        AiProperties.OpenAi openAi = aiProperties.getOpenai();
        if (openAi.getApiKey() == null || openAi.getApiKey().isBlank()) {
            throw new BadRequestException("AI provider API key is missing. Set OPENAI_API_KEY.");
        }

        String endpoint = normalizeBaseUrl(openAi.getBaseUrl()) + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAi.getApiKey());

        Map<String, Object> payload = Map.of(
                "model", openAi.getModel(),
                "temperature", openAi.getTemperature(),
                "max_tokens", openAi.getMaxTokens(),
                "messages", List.of(
                        Map.of("role", "system", "content", aiProperties.getSystemPrompt()),
                        Map.of("role", "user", "content", question)
                )
        );

        try {
            String rawResponse = restTemplate.postForObject(endpoint, new HttpEntity<>(payload, headers), String.class);
            if (rawResponse == null || rawResponse.isBlank()) {
                throw new BadRequestException("AI provider returned an empty response.");
            }

            JsonNode root = objectMapper.readTree(rawResponse);
            String answer = root.path("choices").path(0).path("message").path("content").asText();
            String model = root.path("model").asText(openAi.getModel());

            if (answer == null || answer.isBlank()) {
                throw new BadRequestException("AI provider response did not contain an answer.");
            }

            return AskAiResponse.builder()
                    .answer(answer.trim())
                    .model(model)
                    .build();
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("AI ask request failed", ex);
            throw new BadRequestException("AI request failed. Please try again.");
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "https://api.openai.com/v1";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
