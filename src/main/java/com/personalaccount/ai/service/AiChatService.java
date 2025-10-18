package com.personalaccount.ai.service;

import com.personalaccount.ai.dto.request.AiChatRequest;
import com.personalaccount.ai.dto.response.AiChatResponse;

public interface AiChatService {

    /**
     * AI와 대화
     */
    AiChatResponse chat(Long userId, AiChatRequest request);
}