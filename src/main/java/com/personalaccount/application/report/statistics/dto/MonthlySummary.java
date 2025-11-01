package com.personalaccount.application.report.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * QueryDSL Projection용 DTO
 * Repository에서 GROUP BY 결과 담기
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlySummary {

    private String yearMonth;        // "2025-10"

    private BigDecimal totalIncome;  // 해당 월 총 수입

    private BigDecimal totalExpense; // 해당 월 총 지출
}