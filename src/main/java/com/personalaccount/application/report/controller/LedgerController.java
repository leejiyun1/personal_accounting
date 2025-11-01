package com.personalaccount.application.report.controller;

import com.personalaccount.application.report.dto.response.FinancialStatement;
import com.personalaccount.application.report.service.ReportService;
import com.personalaccount.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final ReportService reportService;

    /**
     * 재무제표 조회 (손익계산서 + 재무상태표)
     */
    @GetMapping("/statement/{bookId}")
    public CommonResponse<FinancialStatement> getFinancialStatement(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long bookId,
            @RequestParam String yearMonth
    ) {
        log.info("재무제표 조회 API: userId={}, bookId={}, yearMonth={}", userId, bookId, yearMonth);
        FinancialStatement result = reportService.getFinancialStatement(userId, bookId, yearMonth);
        return CommonResponse.success(result);
    }

    /**
     * 계정별 원장 조회
     */
    @GetMapping("/account/{bookId}/{accountId}")
    public CommonResponse<Map<String, Object>> getAccountLedger(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long bookId,
            @PathVariable Long accountId,
            @RequestParam String yearMonth
    ) {
        log.info("계정 원장 조회 API: userId={}, bookId={}, accountId={}, yearMonth={}",
                userId, bookId, accountId, yearMonth);
        Map<String, Object> result = reportService.getAccountLedger(userId, bookId, accountId, yearMonth);
        return CommonResponse.success(result);
    }
}