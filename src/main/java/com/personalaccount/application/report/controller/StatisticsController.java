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

    /**
     * 월별 요약 통계 (최근 6개월)
     */
    @GetMapping("/monthly/{bookId}")
    public CommonResponse<List<MonthlySummary>> getMonthlySummary(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long bookId
    ) {
        log.info("월별 요약 조회 API: userId={}, bookId={}", userId, bookId);
        List<MonthlySummary> result = reportService.getMonthlySummary(userId, bookId);
        return CommonResponse.success(result);
    }

    /**
     * 카테고리별 통계
     */
    @GetMapping("/category/{bookId}")
    public CommonResponse<List<CategorySummary>> getCategoryStatistics(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long bookId,
            @RequestParam String yearMonth,
            @RequestParam(defaultValue = "EXPENSE") String type
    ) {
        log.info("카테고리 통계 조회 API: userId={}, bookId={}, yearMonth={}, type={}",
                userId, bookId, yearMonth, type);
        List<CategorySummary> result = reportService.getCategoryStatistics(userId, bookId, yearMonth, type);
        return CommonResponse.success(result);
    }

    /**
     * 계정별 잔액 조회
     */
    @GetMapping("/balances/{bookId}")
    public CommonResponse<List<AccountBalance>> getAccountBalances(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long bookId
    ) {
        log.info("계정별 잔액 조회 API: userId={}, bookId={}", userId, bookId);
        List<AccountBalance> result = reportService.getAccountBalances(userId, bookId);
        return CommonResponse.success(result);
    }
}