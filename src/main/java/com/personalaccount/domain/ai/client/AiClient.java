package com.personalaccount.domain.ai.client;

import com.personalaccount.application.ai.chat.dto.request.GeminiRequest;
import com.personalaccount.application.ai.chat.dto.response.GeminiResponse;

public interface AiClient {

    /**
     * AI 모델에 메시지 전송
     *
     * @param request AI 요청 (컨텍스트 포함)
     * @return AI 응답
     */
    GeminiResponse sendMessage(GeminiRequest request);
}