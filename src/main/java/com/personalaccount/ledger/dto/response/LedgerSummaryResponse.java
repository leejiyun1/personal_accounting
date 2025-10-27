package com.personalaccount.ledger.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LedgerSummaryResponse {

    private IncomeStatement incomeStatement;  // 손익계산서
    private BalanceSheet balanceSheet;        // 재무상태표
}