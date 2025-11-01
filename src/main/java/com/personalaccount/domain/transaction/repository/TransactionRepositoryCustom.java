package com.personalaccount.domain.transaction.repository;

import com.personalaccount.domain.transaction.dto.request.TransactionSearchCondition;
import com.personalaccount.domain.transaction.entity.Transaction;

import java.util.List;

/**
 * Transaction Custom Repository 인터페이스
 */
public interface TransactionRepositoryCustom {

    /**
     * 동적 조건으로 거래 검색
     */
    List<Transaction> searchTransactions(TransactionSearchCondition condition);
}