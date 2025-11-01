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
public class MonthlySummary {
    private String yearMonth;        // "2025-10"
    private BigDecimal income;       // 총 수입
    private BigDecimal expense;      // 총 지출
    private BigDecimal balance;      // 잔액 (수입 - 지출)
}