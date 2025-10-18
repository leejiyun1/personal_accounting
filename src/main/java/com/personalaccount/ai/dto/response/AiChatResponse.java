package com.personalaccount.ai.dto.response;

import com.personalaccount.transaction.dto.response.TransactionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiChatResponse {

    private String conversationId;

    private Boolean needsMoreInfo;  // true면 추가 질문 필요

    private String message;  // AI 응답 메시지

    private List<String> suggestions;  // 선택지 (예: ["급여", "용돈", "부업"])

    private TransactionResponse transaction;  // 거래 완료 시에만
}