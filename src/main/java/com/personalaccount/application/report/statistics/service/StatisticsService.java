package com.personalaccount.application.report.statistics.service;

import com.personalaccount.application.report.statistics.dto.response.AccountBalanceResponse;
import com.personalaccount.application.report.statistics.dto.response.CategoryStatisticsResponse;
import com.personalaccount.application.report.statistics.dto.response.MonthlySummaryResponse;
import com.personalaccount.domain.transaction.entity.TransactionType;

import java.util.List;

public interface StatisticsService {
    List<MonthlySummaryResponse> getMonthlySummary(Long userId, Long bookId);
    
    CategoryStatisticsResponse getCategoryStatistics(
            Long userId,
            Long bookId,
            String yearMonth,
            TransactionType type
    );
    
    List<AccountBalanceResponse> getAccountBalances(Long userId, Long bookId);
}
