package com.personalaccount.statistics.service;

import com.personalaccount.statistics.dto.response.CategoryStatisticsResponse;
import com.personalaccount.statistics.dto.response.MonthlySummaryResponse;
import com.personalaccount.transaction.entity.TransactionType;

import java.util.List;

public interface StatisticsService {

    /**
     * 월별 요약 통계 (최근 6개월)
     */
    List<MonthlySummaryResponse> getMonthlySummary(Long userId, Long bookId);

    /**
     * 카테고리별 통계
     */
    CategoryStatisticsResponse getCategoryStatistics(
            Long userId,
            Long bookId,
            String yearMonth,      // "2025-10"
            TransactionType type   // INCOME or EXPENSE
    );
}