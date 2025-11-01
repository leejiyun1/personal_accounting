package com.personalaccount.application.ai.service;

import com.personalaccount.application.ai.dto.request.AiChatRequest;
import com.personalaccount.application.ai.dto.response.AiChatResponse;

public interface AiChatService {

    /**
     * AI와 대화
     */
    AiChatResponse chat(Long userId, AiChatRequest request);
}