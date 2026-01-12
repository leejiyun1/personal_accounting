package com.personalaccount.domain.ai.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CacheCreateRequest {
    private String model;
    private String systemPrompt;
    private String ttl;
}
