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
@Schema(description = "손익계산서")
public class IncomeStatement {

    @Schema(description = "총 수입", example = "5000000")
    private BigDecimal totalIncome;

    @Schema(description = "총 지출", example = "3000000")
    private BigDecimal totalExpense;

    @Schema(description = "순이익 (수입 - 지출)", example = "2000000")
    private BigDecimal netProfit;

    @Schema(description = "수익률 (%)", example = "40.0")
    private Double profitRate;
}