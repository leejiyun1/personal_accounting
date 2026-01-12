package com.personalaccount.infrastructure.ai.client;

import com.personalaccount.application.ai.chat.dto.request.CachedContentRequest;
import com.personalaccount.application.ai.chat.dto.request.GeminiRequest;
import com.personalaccount.application.ai.chat.dto.response.CachedContentResponse;
import com.personalaccount.application.ai.chat.dto.response.GeminiResponse;
import com.personalaccount.common.exception.custom.AiBadRequestException;
import com.personalaccount.common.exception.custom.AiRateLimitException;
import com.personalaccount.common.exception.custom.AiServiceException;
import com.personalaccount.common.exception.custom.AiTimeoutException;
import com.personalaccount.domain.ai.client.AiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
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
    public Mono<GeminiResponse> sendMessage(GeminiRequest request) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("key", apiKey)
                        .build())
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(createRetrySpec())
                .doOnSuccess(this::logTokenUsage)
                .doOnError(error -> log.error("Gemini API 호출 실패: {}", error.getMessage()))
                .onErrorMap(this::mapToBusinessException);
    }

    @Override
    public Mono<CachedContentResponse> createCachedContent(CachedContentRequest request) {
        log.info("캐시 생성 시작");

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
                .doOnSuccess(response -> log.info("캐시 생성 완료: {}", response.getName()))
                .doOnError(this::logCacheError)
                .onErrorMap(this::mapToBusinessException);
    }

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

    private void logTokenUsage(GeminiResponse response) {
        if (response != null && response.getUsageMetadata() != null) {
            var usage = response.getUsageMetadata();
            log.info("토큰 사용량 - total: {}, prompt: {}, candidates: {}, cached: {}",
                    usage.getTotalTokenCount(),
                    usage.getPromptTokenCount(),
                    usage.getCandidatesTokenCount(),
                    usage.getCachedContentTokenCount());
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