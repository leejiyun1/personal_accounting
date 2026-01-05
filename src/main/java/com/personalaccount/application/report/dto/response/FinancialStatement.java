package com.personalaccount.application.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "재무제표 (손익계산서 + 재무상태표)")
public class FinancialStatement {

    @Schema(description = "손익계산서")
    private IncomeStatement incomeStatement;

    @Schema(description = "재무상태표")
    private BalanceSheet balanceSheet;
}