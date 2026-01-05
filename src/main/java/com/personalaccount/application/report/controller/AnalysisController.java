package com.personalaccount.application.report.controller;

import com.personalaccount.application.report.service.ReportService;
import com.personalaccount.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
@Tag(name = "Analysis", description = "AI 경영 분석 API")
@SecurityRequirement(name = "bearerAuth")
public class AnalysisController {

    private final ReportService reportService;

    @Operation(
            summary = "AI 경영 분석",
            description = """
                    특정 월의 재무 데이터를 AI가 분석하여 인사이트를 제공합니다.
                    
                    **응답 정보:**
                    - summary: 재무제표 요약 (손익계산서 + 재무상태표)
                    - topExpenses: 상위 지출 카테고리 TOP 5
                    - aiComment: AI 분석 코멘트 (준비 중)
                      - overview: 전체 분석 요약
                      - strengths: 잘한 점
                      - warnings: 경고 사항
                      - suggestions: 개선 제안
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "분석 성공"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 장부에 접근 권한이 없습니다"
            )
    })
    @GetMapping("/{bookId}")
    public CommonResponse<Map<String, Object>> getAnalysis(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long bookId,
            @Parameter(description = "조회 월 (YYYY-MM)", example = "2025-01")
            @RequestParam String yearMonth
    ) {
        log.info("GET /api/v1/analysis/{} - userId={}, yearMonth={}", bookId, userId, yearMonth);
        Map<String, Object> result = reportService.getAnalysis(userId, bookId, yearMonth);
        return CommonResponse.success(result);
    }
}