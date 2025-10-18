package com.personalaccount.ai.controller;

import com.personalaccount.ai.dto.request.AiChatRequest;
import com.personalaccount.ai.dto.response.AiChatResponse;
import com.personalaccount.ai.service.AiChatService;
import com.personalaccount.common.dto.CommonResponse;
import com.personalaccount.common.dto.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public ResponseEntity<CommonResponse<AiChatResponse>> chat(
            @RequestHeader("X-User-Id") Long userId,  // TODO: JWT로 변경
            @Valid @RequestBody AiChatRequest request
    ) {
        log.info("AI 대화 API 호출: userId={}, bookId={}", userId, request.getBookId());

        AiChatResponse response = aiChatService.chat(userId, request);

        return ResponseEntity.ok(
                ResponseFactory.success(response)
        );
    }
}