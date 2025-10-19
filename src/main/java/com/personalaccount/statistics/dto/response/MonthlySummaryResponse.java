package com.personalaccount.statistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 월별 요약 응답 (막대 그래프용)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlySummaryResponse {

    private String yearMonth;        // "2025-10"

    private BigDecimal totalIncome;  // 총 수입

    private BigDecimal totalExpense; // 총 지출

    private BigDecimal balance;      // 잔액 (수입 - 지출)
}