package com.personalaccount.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "에러 응답")
public class ErrorResponse {

    @Schema(description = "성공 여부", example = "false")
    private Boolean success;

    @Schema(description = "에러 코드", example = "U001")
    private String errorCode;

    @Schema(description = "에러 메시지", example = "사용자를 찾을 수 없습니다: ID: 1")
    private String message;

    @Schema(description = "타임스탬프", example = "2025-01-05T10:30:00")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}