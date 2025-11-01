package com.personalaccount.application.report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountBalance {
    private Long accountId;          // 계정과목 ID
    private String accountName;      // 계정과목 이름
    private BigDecimal balance;      // 잔액
}