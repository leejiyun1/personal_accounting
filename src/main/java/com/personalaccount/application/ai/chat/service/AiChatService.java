package com.personalaccount.application.ai.chat.service;

import com.personalaccount.application.ai.dto.request.AiChatRequest;
import com.personalaccount.application.ai.dto.response.AiChatResponse;
import reactor.core.publisher.Mono;

public interface AiChatService {

    /**
     * AI와 대화 (논블로킹)
     */
    Mono<AiChatResponse> chat(Long userId, AiChatRequest request);
}
