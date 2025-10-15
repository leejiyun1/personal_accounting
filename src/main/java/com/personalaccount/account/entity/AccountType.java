package com.personalaccount.account.entity;

public enum AccountType {
    ASSET,           // 자산 (1xxx)
    LIABILITY,       // 부채 (2xxx)
    EQUITY,          // 자본 (3xxx)
    REVENUE,         // 수익 (4xxx)
    EXPENSE,         // 비용 (5xxx)
    PAYMENT_METHOD   // 결제수단 (별도 관리)
}