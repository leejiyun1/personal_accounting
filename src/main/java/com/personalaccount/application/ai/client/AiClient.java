package com.personalaccount.application.ai.client;

import com.personalaccount.application.ai.dto.request.GeminiRequest;
import com.personalaccount.application.ai.dto.response.GeminiResponse;

public interface AiClient {

    /**
     * Gemini API에 메시지 전송
     */
    GeminiResponse sendMessage(GeminiRequest request);
}