package com.personalaccount.application.report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialStatement {
    // 손익계산서
    private BigDecimal totalIncome;      // 총 수입
    private BigDecimal totalExpense;     // 총 지출
    private BigDecimal netProfit;        // 순이익

    // 재무상태표
    private BigDecimal totalAssets;      // 총 자산
    private BigDecimal totalLiabilities; // 총 부채
    private BigDecimal totalEquity;      // 총 자본 (자산 - 부채)
}
