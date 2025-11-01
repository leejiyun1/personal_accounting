package com.personalaccount.application.report.analysis.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CategoryExpense {

    private String categoryName;   // 카테고리명
    private BigDecimal amount;     // 금액
    private Double percentage;     // 비율 (%)
}