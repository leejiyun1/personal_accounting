package com.personalaccount.ai.dto.request;

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
public class AiChatRequest {

    private String conversationId;  // null이면 새 대화 시작

    @NotNull(message = "장부 ID는 필수입니다.")
    private Long bookId;

    @NotBlank(message = "메시지는 필수입니다.")
    private String message;
}