package com.personalaccount.application.ai.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenAiRequest {

    private String model;

    private List<Message> messages;

    private Integer maxTokens;

    private Double temperature;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Message {
        private String role;  // "system", "user", "assistant"
        private String content;
    }
}