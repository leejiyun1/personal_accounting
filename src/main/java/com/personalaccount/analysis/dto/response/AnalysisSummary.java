package com.personalaccount.analysis.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AnalysisSummary {

    private BigDecimal totalIncome;    // 총 수입
    private BigDecimal totalExpense;   // 총 지출
    private BigDecimal netProfit;      // 순이익
    private Double profitRate;         // 수익률 (%)
}