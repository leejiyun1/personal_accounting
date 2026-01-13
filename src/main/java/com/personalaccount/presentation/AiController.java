package com.personalaccount.presentation;

import com.personalaccount.application.ai.dto.request.AiChatRequest;
import com.personalaccount.application.ai.dto.response.AiChatResponse;
import com.personalaccount.application.ai.chat.service.AiChatService;
import com.personalaccount.common.dto.CommonResponse;
import com.personalaccount.common.dto.ResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI", description = "AI 대화 기반 거래 생성 API")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    private final AiChatService aiChatService;

    @Operation(
            summary = "AI와 대화하여 거래 생성",
            description = """
                    자연어로 거래 정보를 입력하면 AI가 대화를 통해 거래를 생성합니다.
                    
                    **대화 흐름:**
                    1. 사용자: "오늘 급여 500만원 받았어"
                    2. AI: "어떤 결제수단으로 받으셨나요?"
                    3. 사용자: "은행 계좌로 받았어"
                    4. AI: 거래 생성 완료
                    
                    **필수 정보:**
                    - 거래 타입 (수입/지출)
                    - 금액
                    - 카테고리
                    - 결제수단
                    - 날짜 (생략 시 오늘)
                    
                    정보가 부족하면 AI가 추가 질문을 합니다.
                    
                    **논블로킹 처리:**
                    AI API 호출은 논블로킹으로 처리되어 서버 처리량이 향상됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "대화 성공 (추가 정보 필요 또는 거래 생성 완료)",
                    content = @Content(schema = @Schema(implementation = AiChatResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "세션 만료 또는 유효하지 않은 요청"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 장부에 접근 권한이 없습니다"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "AI 서비스 일시적 오류"
            )
    })
    @PostMapping("/chat")
    public Mono<ResponseEntity<CommonResponse<AiChatResponse>>> chat(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AiChatRequest request
    ) {
        log.info("POST /api/v1/ai/chat - userId={}", userId);
        
        return aiChatService.chat(userId, request)
                .map(response -> ResponseEntity.ok(ResponseFactory.success(response)));
    }
}
