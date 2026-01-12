package com.personalaccount.infrastructure.ai.client;

import com.personalaccount.common.exception.custom.AiBadRequestException;
import com.personalaccount.common.exception.custom.AiRateLimitException;
import com.personalaccount.common.exception.custom.AiServiceException;
import com.personalaccount.common.exception.custom.AiTimeoutException;
import com.personalaccount.domain.ai.client.AiClient;
import com.personalaccount.domain.ai.dto.AiMessageRequest;
import com.personalaccount.domain.ai.dto.AiMessageResponse;
import com.personalaccount.domain.ai.dto.CacheCreateRequest;
import com.personalaccount.domain.ai.dto.CacheCreateResponse;
import com.personalaccount.infrastructure.ai.dto.request.CachedContentRequest;
import com.personalaccount.infrastructure.ai.dto.response.CachedContentResponse;
import com.personalaccount.infrastructure.ai.dto.request.GeminiRequest;
import com.personalaccount.infrastructure.ai.dto.response.GeminiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

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
    public Mono<AiMessageResponse> sendMessage(AiMessageRequest request) {
        GeminiRequest geminiRequest = toGeminiRequest(request);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("key", apiKey)
                        .build())
                .header("Content-Type", "application/json")
                .bodyValue(geminiRequest)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(createRetrySpec())
                .map(this::toAiMessageResponse)
                .doOnSuccess(response -> logTokenUsage(response))
                .doOnError(error -> log.error("Gemini API 호출 실패: {}", error.getMessage()))
                .onErrorMap(this::mapToBusinessException);
    }

    @Override
    public Mono<CacheCreateResponse> createCachedContent(CacheCreateRequest request) {
        log.info("캐시 생성 시작");

        CachedContentRequest cachedContentRequest = toCachedContentRequest(request);

        return WebClient.create(cacheUrl)
                .post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("key", apiKey)
                        .build())
                .header("Content-Type", "application/json")
                .bodyValue(cachedContentRequest)
                .retrieve()
                .bodyToMono(CachedContentResponse.class)
                .timeout(Duration.ofMillis(timeout))
                .map(this::toCacheCreateResponse)
                .doOnSuccess(response -> log.info("캐시 생성 완료: {}", response.getCacheName()))
                .doOnError(this::logCacheError)
                .onErrorMap(this::mapToBusinessException);
    }

    // === Domain DTO <-> Infrastructure DTO 변환 ===

    private GeminiRequest toGeminiRequest(AiMessageRequest request) {
        return GeminiRequest.builder()
                .cachedContent(request.getCachedContentName())
                .contents(List.of(GeminiRequest.Content.builder()
                        .parts(List.of(GeminiRequest.Part.builder()
                                .text(request.getConversationText())
                                .build()))
                        .build()))
                .build();
    }

    private AiMessageResponse toAiMessageResponse(GeminiResponse response) {
        String message = response.getCandidates().getFirst()
                .getContent()
                .getParts().getFirst()
                .getText();

        AiMessageResponse.AiMessageResponseBuilder builder = AiMessageResponse.builder()
                .message(message);

        if (response.getUsageMetadata() != null) {
            var usage = response.getUsageMetadata();
            builder.promptTokenCount(usage.getPromptTokenCount())
                    .candidatesTokenCount(usage.getCandidatesTokenCount())
                    .totalTokenCount(usage.getTotalTokenCount())
                    .cachedContentTokenCount(usage.getCachedContentTokenCount());
        }

        return builder.build();
    }

    private CachedContentRequest toCachedContentRequest(CacheCreateRequest request) {
        return CachedContentRequest.builder()
                .model(request.getModel())
                .systemInstruction(CachedContentRequest.SystemInstruction.builder()
                        .parts(List.of(CachedContentRequest.Part.builder()
                                .text(request.getSystemPrompt())
                                .build()))
                        .build())
                .ttl(request.getTtl())
                .build();
    }

    private CacheCreateResponse toCacheCreateResponse(CachedContentResponse response) {
        return CacheCreateResponse.builder()
                .cacheName(response.getName())
                .expireTime(response.getExpireTime())
                .build();
    }

    // === Retry & Error Handling ===

    private Retry createRetrySpec() {
        return Retry.backoff(maxRetry, Duration.ofSeconds(1))
                .filter(this::isRetryable)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    Throwable failure = retrySignal.failure();
                    log.error("Gemini API 재시도 횟수 초과: {}", retrySignal.totalRetries());

                    if (failure instanceof WebClientResponseException ex) {
                        return mapHttpException(ex);
                    }
                    return new AiServiceException("AI 서비스 호출 실패: 재시도 횟수 초과");
                });
    }

    private boolean isRetryable(Throwable throwable) {
        if (throwable instanceof WebClientResponseException ex) {
            int statusCode = ex.getStatusCode().value();
            return statusCode == 503 || statusCode == 429 || statusCode == 500;
        }
        return throwable instanceof TimeoutException;
    }

    private Throwable mapToBusinessException(Throwable throwable) {
        if (throwable instanceof AiServiceException ||
                throwable instanceof AiRateLimitException ||
                throwable instanceof AiBadRequestException ||
                throwable instanceof AiTimeoutException) {
            return throwable;
        }

        if (throwable instanceof TimeoutException) {
            return new AiTimeoutException("AI 응답 시간이 초과되었습니다 (30초)");
        }

        if (throwable instanceof WebClientResponseException ex) {
            return mapHttpException(ex);
        }

        return new AiServiceException("AI 서비스 호출에 실패했습니다", throwable);
    }

    private Throwable mapHttpException(WebClientResponseException ex) {
        HttpStatusCode status = ex.getStatusCode();
        String responseBody = ex.getResponseBodyAsString();

        log.error("Gemini API 에러 - Status: {}, Body: {}", status, responseBody);

        return switch (status.value()) {
            case 400 -> new AiBadRequestException(parseErrorMessage(responseBody, "잘못된 요청입니다"));
            case 401, 403 -> new AiServiceException("API 인증에 실패했습니다. API 키를 확인해주세요.");
            case 429 -> new AiRateLimitException("API 할당량을 초과했습니다. 잠시 후 다시 시도해주세요.");
            case 500 -> new AiServiceException("AI 서버 내부 오류가 발생했습니다");
            case 503 -> new AiServiceException("AI 서비스가 일시적으로 불가능합니다");
            default -> new AiServiceException("AI 서비스 오류: " + status.value());
        };
    }

    private String parseErrorMessage(String responseBody, String defaultMessage) {
        try {
            if (responseBody != null && responseBody.contains("message")) {
                int start = responseBody.indexOf("\"message\"") + 11;
                int end = responseBody.indexOf("\"", start);
                if (start > 10 && end > start) {
                    return responseBody.substring(start, end);
                }
            }
        } catch (Exception e) {
            log.debug("에러 메시지 파싱 실패: {}", e.getMessage());
        }
        return defaultMessage;
    }

    private void logTokenUsage(AiMessageResponse response) {
        if (response != null && response.getTotalTokenCount() != null) {
            log.info("토큰 사용량 - total: {}, prompt: {}, candidates: {}, cached: {}",
                    response.getTotalTokenCount(),
                    response.getPromptTokenCount(),
                    response.getCandidatesTokenCount(),
                    response.getCachedContentTokenCount());
        }
    }

    private void logCacheError(Throwable error) {
        if (error instanceof WebClientResponseException ex) {
            log.error("캐시 생성 실패 - Status: {}, Body: {}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());
        } else {
            log.error("캐시 생성 실패", error);
        }
    }
}
