package com.personalaccount.domain.ai.client;

import com.personalaccount.domain.ai.dto.AiMessageRequest;
import com.personalaccount.domain.ai.dto.AiMessageResponse;
import com.personalaccount.domain.ai.dto.CacheCreateRequest;
import com.personalaccount.domain.ai.dto.CacheCreateResponse;
import reactor.core.publisher.Mono;

public interface AiClient {

    Mono<AiMessageResponse> sendMessage(AiMessageRequest request);

    Mono<CacheCreateResponse> createCachedContent(CacheCreateRequest request);
}
