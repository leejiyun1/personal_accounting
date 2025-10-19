package com.personalaccount.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * QueryDSL Projection용 DTO
 * 카테고리별 집계 결과
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryStatistics {

    private Long categoryId;         // 계정과목 ID

    private String categoryCode;     // 계정과목 코드 (5100)

    private String categoryName;     // 계정과목 이름 (식비)

    private BigDecimal amount;       // 해당 카테고리 합계
}