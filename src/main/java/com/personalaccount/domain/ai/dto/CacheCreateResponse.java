package com.personalaccount.domain.ai.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CacheCreateResponse {
    private String cacheName;
    private String expireTime;
}
