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

    @GetMapping("/statement/{bookId}")
    public CommonResponse<FinancialStatement> getFinancialStatement(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long bookId,
            @RequestParam String yearMonth
    ) {
        log.info("GET /api/v1/ledger/statement/{} - userId={}", bookId, userId);
        FinancialStatement result = reportService.getFinancialStatement(userId, bookId, yearMonth);
        return CommonResponse.success(result);
    }

    @GetMapping("/account/{bookId}/{accountId}")
    public CommonResponse<Map<String, Object>> getAccountLedger(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long bookId,
            @PathVariable Long accountId,
            @RequestParam String yearMonth
    ) {
        log.info("GET /api/v1/ledger/account/{}/{} - userId={}", bookId, accountId, userId);
        Map<String, Object> result = reportService.getAccountLedger(userId, bookId, accountId, yearMonth);
        return CommonResponse.success(result);
    }
}