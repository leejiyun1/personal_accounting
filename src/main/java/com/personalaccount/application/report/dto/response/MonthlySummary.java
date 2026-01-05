package com.personalaccount.application.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "월별 요약 통계")
public class MonthlySummary {

    @Schema(description = "연월 (YYYY-MM)", example = "2025-01")
    private String yearMonth;

    @Schema(description = "총 수입", example = "5000000")
    private BigDecimal income;

    @Schema(description = "총 지출", example = "3000000")
    private BigDecimal expense;

    @Schema(description = "잔액 (수입 - 지출)", example = "2000000")
    private BigDecimal balance;
}