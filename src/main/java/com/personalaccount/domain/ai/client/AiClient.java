package com.personalaccount.domain.ai.client;

import com.personalaccount.application.ai.chat.dto.request.CachedContentRequest;
import com.personalaccount.application.ai.chat.dto.request.GeminiRequest;
import com.personalaccount.application.ai.chat.dto.response.CachedContentResponse;
import com.personalaccount.application.ai.chat.dto.response.GeminiResponse;
import reactor.core.publisher.Mono;

public interface AiClient {

    Mono<GeminiResponse> sendMessage(GeminiRequest request);

    Mono<CachedContentResponse> createCachedContent(CachedContentRequest request);
}