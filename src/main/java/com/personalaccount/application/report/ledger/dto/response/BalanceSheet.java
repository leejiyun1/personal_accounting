package com.personalaccount.application.report.ledger.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class BalanceSheet {

    private BigDecimal totalAssets;      // 총 자산
    private BigDecimal totalLiabilities; // 총 부채
    private BigDecimal totalEquity;      // 총 자본 (자산 - 부채)
}