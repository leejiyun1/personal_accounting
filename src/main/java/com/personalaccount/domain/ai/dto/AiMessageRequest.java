package com.personalaccount.domain.ai.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiMessageRequest {
    private String cachedContentName;
    private String conversationText;
}
