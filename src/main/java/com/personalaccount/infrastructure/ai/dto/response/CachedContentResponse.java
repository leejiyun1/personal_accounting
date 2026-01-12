package com.personalaccount.infrastructure.ai.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CachedContentResponse {
    private String name;
    private String expireTime;
}
