package com.personalaccount.ai.client;

import com.personalaccount.ai.dto.request.GeminiRequest;
import com.personalaccount.ai.dto.response.GeminiResponse;

public interface AiClient {

    /**
     * Gemini API에 메시지 전송
     */
    GeminiResponse sendMessage(GeminiRequest request);
}