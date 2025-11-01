package com.personalaccount.application.report.statistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AccountBalanceResponse {
    private Long accountId;
    private String accountName;
    private Long balance;  // 현재 잔액 (차변 - 대변)
}
