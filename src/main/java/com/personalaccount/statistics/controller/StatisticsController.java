package com.personalaccount.statistics.controller;

import com.personalaccount.common.dto.CommonResponse;
import com.personalaccount.common.dto.ResponseFactory;
import com.personalaccount.statistics.dto.response.CategoryStatisticsResponse;
import com.personalaccount.statistics.dto.response.MonthlySummaryResponse;
import com.personalaccount.statistics.service.StatisticsService;
import com.personalaccount.transaction.entity.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 월별 요약 통계 (최근 6개월)
     * GET /api/v1/statistics/summary?bookId=1
     */
    @GetMapping("/summary")
    public ResponseEntity<CommonResponse<List<MonthlySummaryResponse>>> getMonthlySummary(
            @RequestHeader("X-User-Id") Long userId,  // TODO: JWT로 변경
            @RequestParam Long bookId
    ) {
        log.info("월별 요약 API 호출: userId={}, bookId={}", userId, bookId);

        List<MonthlySummaryResponse> response = statisticsService.getMonthlySummary(userId, bookId);

        return ResponseEntity.ok(
                ResponseFactory.success(response)
        );
    }

    /**
     * 카테고리별 통계
     * GET /api/v1/statistics/category?bookId=1&yearMonth=2025-10&type=EXPENSE
     */
    @GetMapping("/category")
    public ResponseEntity<CommonResponse<CategoryStatisticsResponse>> getCategoryStatistics(
            @RequestHeader("X-User-Id") Long userId,  // TODO: JWT로 변경
            @RequestParam Long bookId,
            @RequestParam String yearMonth,  // "2025-10"
            @RequestParam TransactionType type  // INCOME or EXPENSE
    ) {
        log.info("카테고리별 통계 API 호출: userId={}, bookId={}, yearMonth={}, type={}",
                userId, bookId, yearMonth, type);

        CategoryStatisticsResponse response = statisticsService.getCategoryStatistics(
                userId, bookId, yearMonth, type
        );

        return ResponseEntity.ok(
                ResponseFactory.success(response)
        );
    }
}