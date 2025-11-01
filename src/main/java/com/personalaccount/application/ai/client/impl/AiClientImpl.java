package com.personalaccount.application.ai.client.impl;

import com.personalaccount.application.ai.client.AiClient;
import com.personalaccount.application.ai.dto.request.GeminiRequest;
import com.personalaccount.application.ai.dto.response.GeminiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
public class AiClientImpl implements AiClient {

    private final WebClient webClient;
    private final String apiKey;
    private final int maxRetry;
    private final long timeout;

    public AiClientImpl(
            WebClient.Builder webClientBuilder,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.api-url}") String apiUrl,
            @Value("${gemini.max-retry:3}") int maxRetry,
            @Value("${gemini.timeout:30000}") long timeout
    ) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
        this.apiKey = apiKey;
        this.maxRetry = maxRetry;
        this.timeout = timeout;
    }

    @Override
    public GeminiResponse sendMessage(GeminiRequest request) {
        log.debug("Gemini API 호출: {}", request);

        try {
            return webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("key", apiKey)
                            .build())
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .retryWhen(Retry.backoff(maxRetry, Duration.ofSeconds(1))
                            .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable
                                    || throwable instanceof WebClientResponseException.TooManyRequests)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                log.error("Gemini API 재시도 횟수 초과: {}", retrySignal.totalRetries());
                                return new RuntimeException("AI 서비스 호출 실패: 재시도 횟수 초과");
                            }))
                    .doOnError(error -> log.error("Gemini API 호출 실패", error))
                    .block();
        } catch (Exception e) {
            log.error("Gemini API 호출 실패", e);
            throw new RuntimeException("AI 서비스 호출에 실패했습니다.", e);
        }
    }
}