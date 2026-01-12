package com.personalaccount.application.ai.dto.response;

import com.personalaccount.domain.transaction.dto.response.TransactionResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "AI 대화 응답")
public class AiChatResponse {

    @Schema(
            description = "대화 ID (다음 대화에 사용)",
            example = "conv-abc123"
    )
    private String conversationId;

    @Schema(
            description = "추가 정보 필요 여부 (true: 추가 질문 필요, false: 거래 생성 완료)",
            example = "true"
    )
    private Boolean needsMoreInfo;

    @Schema(
            description = "AI 응답 메시지",
            example = "어떤 결제수단으로 받으셨나요?"
    )
    private String message;

    @Schema(
            description = "선택지 제안 (선택적)",
            example = "[\"현금\", \"은행\", \"카드\"]"
    )
    private List<String> suggestions;

    @Schema(
            description = "생성된 거래 정보 (거래 완료 시에만)"
    )
    private TransactionResponse transaction;
}
