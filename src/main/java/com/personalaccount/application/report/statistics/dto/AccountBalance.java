package com.personalaccount.application.report.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AccountBalance {
    private Long accountId;
    private String accountName;
    private BigDecimal balance;
}
