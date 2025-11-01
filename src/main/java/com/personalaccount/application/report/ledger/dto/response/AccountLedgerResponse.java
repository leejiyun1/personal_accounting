package com.personalaccount.application.report.ledger.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class AccountLedgerResponse {

    private String accountName;              // 계정과목명
    private BigDecimal openingBalance;       // 기초 잔액
    private BigDecimal closingBalance;       // 기말 잔액
    private List<LedgerEntry> entries;       // 거래 내역

    @Getter
    @Builder
    public static class LedgerEntry {
        private LocalDate date;              // 거래일
        private String description;          // 적요 (메모)
        private BigDecimal debit;            // 차변
        private BigDecimal credit;           // 대변
        private BigDecimal balance;          // 잔액
    }
}