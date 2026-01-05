package com.personalaccount.application.report.controller;

import com.personalaccount.application.report.dto.response.FinancialStatement;
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
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
@Tag(name = "Ledger", description = "재무제표 및 원장 API")
@SecurityRequirement(name = "bearerAuth")
public class LedgerController {

    private final ReportService reportService;

    @Operation(
            summary = "재무제표 조회",
            description = """
                    특정 월의 손익계산서와 재무상태표를 조회합니다.
                    
                    **손익계산서 (Income Statement):**
                    - 총수입 (totalIncome)
                    - 총지출 (totalExpense)
                    - 순이익 (netProfit = 수입 - 지출)
                    - 수익률 (profitRate = 순이익 / 수입 × 100)
                    
                    **재무상태표 (Balance Sheet):**
                    - 총자산 (totalAssets)
                    - 총부채 (totalLiabilities)
                    - 총자본 (totalEquity = 자산 - 부채)
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = FinancialStatement.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 장부에 접근 권한이 없습니다"
            )
    })
    @GetMapping("/statement/{bookId}")
    public CommonResponse<FinancialStatement> getFinancialStatement(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long bookId,
            @Parameter(description = "조회 월 (YYYY-MM)", example = "2025-01")
            @RequestParam String yearMonth
    ) {
        log.info("GET /api/v1/ledger/statement/{} - userId={}, yearMonth={}", bookId, userId, yearMonth);
        FinancialStatement result = reportService.getFinancialStatement(userId, bookId, yearMonth);
        return CommonResponse.success(result);
    }

    @Operation(
            summary = "계정별 원장 조회",
            description = """
                    특정 계정의 월별 거래 내역을 원장 형태로 조회합니다.
                    
                    **응답 정보:**
                    - accountName: 계정과목 이름
                    - openingBalance: 기초 잔액
                    - entries: 거래 내역 (날짜, 적요, 차변, 대변, 잔액)
                    - closingBalance: 기말 잔액
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 장부에 접근 권한이 없습니다"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "계정과목을 찾을 수 없습니다"
            )
    })
    @GetMapping("/account/{bookId}/{accountId}")
    public CommonResponse<Map<String, Object>> getAccountLedger(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long bookId,
            @PathVariable Long accountId,
            @Parameter(description = "조회 월 (YYYY-MM)", example = "2025-01")
            @RequestParam String yearMonth
    ) {
        log.info("GET /api/v1/ledger/account/{}/{} - userId={}, yearMonth={}",
                bookId, accountId, userId, yearMonth);
        Map<String, Object> result = reportService.getAccountLedger(userId, bookId, accountId, yearMonth);
        return CommonResponse.success(result);
    }
}