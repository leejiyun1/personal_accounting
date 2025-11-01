package com.personalaccount.application.report.service;

import com.personalaccount.application.report.dto.response.*;
import java.util.List;
import java.util.Map;

public interface ReportService {

    // 통계
    List<MonthlySummary> getMonthlySummary(Long userId, Long bookId);
    List<CategorySummary> getCategoryStatistics(Long userId, Long bookId, String yearMonth, String type);
    List<AccountBalance> getAccountBalances(Long userId, Long bookId);

    // 재무제표
    FinancialStatement getFinancialStatement(Long userId, Long bookId, String yearMonth);

    // 계정 원장
    Map<String, Object> getAccountLedger(Long userId, Long bookId, Long accountId, String yearMonth);

    // AI 분석
    Map<String, Object> getAnalysis(Long userId, Long bookId, String yearMonth);
}