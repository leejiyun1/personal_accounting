package com.personalaccount.application.report.controller;

import com.personalaccount.application.report.dto.response.AccountBalance;
import com.personalaccount.application.report.dto.response.CategorySummary;
import com.personalaccount.application.report.dto.response.MonthlySummary;
import com.personalaccount.application.report.service.ReportService;
import com.personalaccount.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final ReportService reportService;

    @GetMapping("/monthly/{bookId}")
    public CommonResponse<List<MonthlySummary>> getMonthlySummary(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long bookId
    ) {
        log.info("GET /api/v1/statistics/monthly/{} - userId={}", bookId, userId);
        List<MonthlySummary> result = reportService.getMonthlySummary(userId, bookId);
        return CommonResponse.success(result);
    }

    @GetMapping("/category/{bookId}")
    public CommonResponse<List<CategorySummary>> getCategoryStatistics(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long bookId,
            @RequestParam String yearMonth,
            @RequestParam(defaultValue = "EXPENSE") String type
    ) {
        log.info("GET /api/v1/statistics/category/{} - userId={}", bookId, userId);
        List<CategorySummary> result = reportService.getCategoryStatistics(userId, bookId, yearMonth, type);
        return CommonResponse.success(result);
    }

    @GetMapping("/balances/{bookId}")
    public CommonResponse<List<AccountBalance>> getAccountBalances(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long bookId
    ) {
        log.info("GET /api/v1/statistics/balances/{} - userId={}", bookId, userId);
        List<AccountBalance> result = reportService.getAccountBalances(userId, bookId);
        return CommonResponse.success(result);
    }
}