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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/summary")
    public ResponseEntity<CommonResponse<List<MonthlySummaryResponse>>> getMonthlySummary(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long bookId
    ) {
        log.info("월별 요약 API 호출: userId={}, bookId={}", userId, bookId);

        List<MonthlySummaryResponse> response = statisticsService.getMonthlySummary(userId, bookId);

        return ResponseEntity.ok(
                ResponseFactory.success(response)
        );
    }

    @GetMapping("/category")
    public ResponseEntity<CommonResponse<CategoryStatisticsResponse>> getCategoryStatistics(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long bookId,
            @RequestParam String yearMonth,
            @RequestParam TransactionType type
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
