package com.personalaccount.ai.client.impl;

import com.personalaccount.ai.client.AiClient;
import com.personalaccount.ai.dto.request.GeminiRequest;
import com.personalaccount.ai.dto.response.GeminiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiClientImpl implements AiClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.api-url}")
    private String apiUrl;

    @Override
    public GeminiResponse sendMessage(GeminiRequest request) {
        log.debug("Gemini API 호출: {}", request);

        try {
            return webClientBuilder.build()
                    .post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Gemini API 호출 실패", e);
            throw new RuntimeException("AI 서비스 호출에 실패했습니다.", e);
        }
    }
}