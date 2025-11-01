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
public class CategorySummary {
    private Long categoryId;         // 계정과목 ID
    private String categoryCode;     // 계정과목 코드 (5100)
    private String categoryName;     // 계정과목 이름 (식비)
    private BigDecimal amount;       // 금액
    private Double percentage;       // 비율 (%)
}