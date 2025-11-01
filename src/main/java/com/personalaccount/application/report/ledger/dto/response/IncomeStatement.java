package com.personalaccount.application.report.ledger.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class IncomeStatement {

    private BigDecimal totalIncome;      // 총 수입
    private BigDecimal totalExpense;     // 총 지출
    private BigDecimal netIncome;        // 순이익 (수입 - 지출)
}