package com.personalaccount.application.report.controller;

import com.personalaccount.application.report.dto.response.AccountBalance;
import com.personalaccount.application.report.dto.response.CategorySummary;
import com.personalaccount.application.report.dto.response.MonthlySummary;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "통계 및 집계 API")
@SecurityRequirement(name = "bearerAuth")
public class StatisticsController {

    private final ReportService reportService;

    @Operation(
            summary = "월별 요약 통계 조회",
            description = """
                    최근 6개월의 월별 수입/지출 요약을 조회합니다.
                    
                    **응답 정보:**
                    - yearMonth: 연월 (YYYY-MM)
                    - income: 총 수입
                    - expense: 총 지출
                    - balance: 잔액 (수입 - 지출)
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MonthlySummary.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 장부에 접근 권한이 없습니다"
            )
    })
    @GetMapping("/monthly/{bookId}")
    public CommonResponse<List<MonthlySummary>> getMonthlySummary(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long bookId
    ) {
        log.info("GET /api/v1/statistics/monthly/{} - userId={}", bookId, userId);
        List<MonthlySummary> result = reportService.getMonthlySummary(userId, bookId);
        return CommonResponse.success(result);
    }

    @Operation(
            summary = "카테고리별 통계 조회",
            description = """
                    특정 월의 카테고리별 지출/수입 통계를 조회합니다.
                    
                    **응답 정보:**
                    - categoryName: 카테고리 이름
                    - amount: 금액
                    - percentage: 비율 (%)
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CategorySummary.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 장부에 접근 권한이 없습니다"
            )
    })
    @GetMapping("/category/{bookId}")
    public CommonResponse<List<CategorySummary>> getCategoryStatistics(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long bookId,
            @Parameter(description = "조회 월 (YYYY-MM)", example = "2025-01")
            @RequestParam String yearMonth,
            @Parameter(description = "거래 타입 (EXPENSE: 지출, INCOME: 수입)", example = "EXPENSE")
            @RequestParam(defaultValue = "EXPENSE") String type
    ) {
        log.info("GET /api/v1/statistics/category/{} - userId={}, yearMonth={}, type={}",
                bookId, userId, yearMonth, type);
        List<CategorySummary> result = reportService.getCategoryStatistics(userId, bookId, yearMonth, type);
        return CommonResponse.success(result);
    }

    @Operation(
            summary = "계정별 잔액 조회",
            description = """
                    모든 결제수단(현금, 은행, 카드 등)의 현재 잔액을 조회합니다.
                    
                    **응답 정보:**
                    - accountName: 계정과목 이름
                    - balance: 현재 잔액
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AccountBalance.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 장부에 접근 권한이 없습니다"
            )
    })
    @GetMapping("/balances/{bookId}")
    public CommonResponse<List<AccountBalance>> getAccountBalances(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long bookId
    ) {
        log.info("GET /api/v1/statistics/balances/{} - userId={}", bookId, userId);
        List<AccountBalance> result = reportService.getAccountBalances(userId, bookId);
        return CommonResponse.success(result);
    }
}