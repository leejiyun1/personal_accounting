package com.personalaccount.application.ai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "AI 대화 요청")
public class AiChatRequest {

    @Schema(
            description = "대화 ID (새 대화 시작 시 null)",
            example = "conv-abc123"
    )
    private String conversationId;

    @Schema(
            description = "장부 ID",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "장부 ID는 필수입니다.")
    private Long bookId;

    @Schema(
            description = "사용자 메시지",
            example = "오늘 급여 500만원 은행 계좌로 받았어",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "메시지는 필수입니다.")
    private String message;
}
