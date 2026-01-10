package com.personalaccount.infrastructure.ai.client;

import com.personalaccount.application.ai.chat.dto.request.CachedContentRequest;
import com.personalaccount.application.ai.chat.dto.request.GeminiRequest;
import com.personalaccount.application.ai.chat.dto.response.CachedContentResponse;
import com.personalaccount.application.ai.chat.dto.response.GeminiResponse;
import com.personalaccount.common.exception.custom.AiServiceException;
import com.personalaccount.domain.ai.client.AiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
public class GeminiClientImpl implements AiClient {

    private final WebClient webClient;
    private final String apiKey;
    private final String cacheUrl;
    private final int maxRetry;
    private final long timeout;

    public GeminiClientImpl(
            WebClient.Builder webClientBuilder,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.api-url}") String apiUrl,
            @Value("${gemini.cache-url}") String cacheUrl,
            @Value("${gemini.max-retry:3}") int maxRetry,
            @Value("${gemini.timeout:30000}") long timeout
    ) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
        this.apiKey = apiKey;
        this.cacheUrl = cacheUrl;
        this.maxRetry = maxRetry;
        this.timeout = timeout;
    }

    @Override
    public Mono<GeminiResponse> sendMessage(GeminiRequest request) {
        log.debug("Gemini API 호출: {}", request);

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
                        .filter(throwable ->
                                throwable instanceof WebClientResponseException.ServiceUnavailable ||
                                        throwable instanceof WebClientResponseException.TooManyRequests)
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            log.error("Gemini API 재시도 횟수 초과: {}", retrySignal.totalRetries());
                            return new AiServiceException("AI 서비스 호출 실패: 재시도 횟수 초과");
                        }))
                .doOnSuccess(response -> {
                    if (response != null && response.getUsageMetadata() != null) {
                        log.info("Gemini API 토큰 사용량 - input: {}, output: {}, total: {}",
                                response.getUsageMetadata().getPromptTokenCount(),
                                response.getUsageMetadata().getCandidatesTokenCount(),
                                response.getUsageMetadata().getTotalTokenCount());
                    }
                })
                .doOnError(error -> log.error("Gemini API 호출 실패", error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof AiServiceException) {
                        return throwable;
                    }
                    return new AiServiceException("AI 서비스 호출에 실패했습니다.", throwable);
                });
    }

    @Override
    public Mono<CachedContentResponse> createCachedContent(CachedContentRequest request) {
        log.info("Gemini 캐시 생성 시작: {}", request);

        return WebClient.create(cacheUrl)
                .post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("key", apiKey)
                        .build())
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CachedContentResponse.class)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(response -> log.info("Gemini 캐시 생성 완료: {}", response.getName()))
                .doOnError(error -> {
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException ex = (WebClientResponseException) error;
                        log.error("Gemini 캐시 생성 실패 - Status: {}, Body: {}",
                                ex.getStatusCode(), ex.getResponseBodyAsString());
                    } else {
                        log.error("Gemini 캐시 생성 실패", error);
                    }
                })
                .onErrorMap(throwable -> new AiServiceException("캐시 생성에 실패했습니다.", throwable));
    }
}