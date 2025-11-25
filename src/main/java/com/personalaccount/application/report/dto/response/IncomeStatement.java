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
public class IncomeStatement {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netProfit;
    private Double profitRate;
}