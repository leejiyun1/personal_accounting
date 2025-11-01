package com.personalaccount.application.report.dto.response;

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
public class TransactionEntry {
    private LocalDate date;              // 거래일
    private String description;          // 적요 (메모)
    private BigDecimal debit;            // 차변
    private BigDecimal credit;           // 대변
    private BigDecimal balance;          // 잔액
}