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
@Schema(description = "재무상태표")
public class BalanceSheet {

    @Schema(description = "총 자산", example = "10000000")
    private BigDecimal totalAssets;

    @Schema(description = "총 부채", example = "3000000")
    private BigDecimal totalLiabilities;

    @Schema(description = "총 자본 (자산 - 부채)", example = "7000000")
    private BigDecimal totalEquity;
}