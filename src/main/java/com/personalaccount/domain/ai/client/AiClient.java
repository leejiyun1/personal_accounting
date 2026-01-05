package com.personalaccount.domain.ai.client;

import com.personalaccount.application.ai.chat.dto.request.GeminiRequest;
import com.personalaccount.application.ai.chat.dto.response.GeminiResponse;
import reactor.core.publisher.Mono;

public interface AiClient {

    /**
     * AI 모델에 메시지 전송 (비동기)
     */
    Mono<GeminiResponse> sendMessage(GeminiRequest request);
}