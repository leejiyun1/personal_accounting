package com.personalaccount.application.ai.controller;

import com.personalaccount.application.ai.dto.request.AiChatRequest;
import com.personalaccount.application.ai.dto.response.AiChatResponse;
import com.personalaccount.application.ai.service.AiChatService;
import com.personalaccount.common.dto.CommonResponse;
import com.personalaccount.common.dto.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public ResponseEntity<CommonResponse<AiChatResponse>> chat(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AiChatRequest request
    ) {
        log.info("AI 대화 API 호출: userId={}, bookId={}", userId, request.getBookId());

        AiChatResponse response = aiChatService.chat(userId, request);

        return ResponseEntity.ok(
                ResponseFactory.success(response)
        );
    }
}