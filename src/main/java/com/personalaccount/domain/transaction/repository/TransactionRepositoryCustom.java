package com.personalaccount.domain.transaction.repository;

import com.personalaccount.domain.transaction.dto.request.TransactionSearchCondition;
import com.personalaccount.domain.transaction.entity.Transaction;

import java.util.List;

/**
 * Transaction Custom Repository 인터페이스
 * - QueryDSL 동적 쿼리
 */
public interface TransactionRepositoryCustom {

    /**
     * 동적 조건으로 거래 검색
     * - bookId: 필수
     * - type: 선택 (null이면 전체)
     * - startDate/endDate: 선택 (기간 필터)
     * - keyword: 선택 (메모 검색)
     */
    List<Transaction> searchTransactions(TransactionSearchCondition condition);
}