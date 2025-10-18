package com.personalaccount.ai.client.impl;

import com.personalaccount.ai.client.AiClient;
import com.personalaccount.ai.dto.request.OpenAiRequest;
import com.personalaccount.ai.dto.response.OpenAiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiClient implements AiClient {

    private final WebClient openAiWebClient;

    @Override
    public OpenAiResponse sendMessage(OpenAiRequest request) {
        log.debug("OpenAI API 호출: model={}, messages={}",
                request.getModel(), request.getMessages().size());

        try {
            OpenAiResponse response = openAiWebClient
                    .post()
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OpenAiResponse.class)
                    .block();

            log.debug("OpenAI API 응답 받음: tokens={}",
                    response.getUsage().getTotalTokens());

            return response;

        } catch (Exception e) {
            log.error("OpenAI API 호출 실패", e);
            throw new RuntimeException("AI 서비스 호출에 실패했습니다.", e);
        }
    }
}