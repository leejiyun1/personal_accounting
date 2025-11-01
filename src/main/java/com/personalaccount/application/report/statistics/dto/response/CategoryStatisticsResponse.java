package com.personalaccount.application.report.statistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 카테고리별 통계 전체 응답 (도넛 차트용)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryStatisticsResponse {

    private String yearMonth;           // "2025-10"

    private String type;                // "INCOME" or "EXPENSE"

    private BigDecimal totalAmount;     // 전체 합계

    private List<CategoryItem> categories;  // 카테고리 목록

    /**
     * 개별 카테고리 정보
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryItem {

        private Long categoryId;        // 계정과목 ID

        private String categoryCode;    // 계정과목 코드

        private String categoryName;    // 계정과목 이름

        private BigDecimal amount;      // 금액

        private Double percentage;      // 비율 (%)
    }
}