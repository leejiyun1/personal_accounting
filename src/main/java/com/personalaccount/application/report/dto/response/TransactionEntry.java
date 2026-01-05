package com.personalaccount.application.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "거래 내역 (원장용)")
public class TransactionEntry {

    @Schema(description = "거래일", example = "2025-01-05")
    private LocalDate date;

    @Schema(description = "적요 (메모)", example = "월급 입금")
    private String description;

    @Schema(description = "차변 금액", example = "500000")
    private BigDecimal debit;

    @Schema(description = "대변 금액", example = "0")
    private BigDecimal credit;

    @Schema(description = "잔액", example = "2500000")
    private BigDecimal balance;
}