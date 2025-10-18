package com.personalaccount.ai.client;

import com.personalaccount.ai.dto.request.OpenAiRequest;
import com.personalaccount.ai.dto.response.OpenAiResponse;

public interface AiClient {

    /**
     * OpenAI API에 메시지 전송
     */
    OpenAiResponse sendMessage(OpenAiRequest request);
}