package com.personalaccount.infrastructure.ai.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CachedContentRequest {
    private String model;
    private SystemInstruction systemInstruction;
    private String ttl;

    @Getter
    @Builder
    public static class SystemInstruction {
        private List<Part> parts;
    }

    @Getter
    @Builder
    public static class Part {
        private String text;
    }
}
