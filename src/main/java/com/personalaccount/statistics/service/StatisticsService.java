package com.personalaccount.statistics.service;

import com.personalaccount.statistics.dto.response.AccountBalanceResponse;
import com.personalaccount.statistics.dto.response.CategoryStatisticsResponse;
import com.personalaccount.statistics.dto.response.MonthlySummaryResponse;
import com.personalaccount.transaction.entity.TransactionType;

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
