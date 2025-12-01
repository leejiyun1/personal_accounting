package com.personalaccount.domain.transaction.service;

import com.personalaccount.domain.transaction.dto.request.TransactionCreateRequest;
import com.personalaccount.domain.transaction.dto.request.TransactionUpdateRequest;
import com.personalaccount.domain.transaction.dto.response.TransactionDetailResponse;
import com.personalaccount.domain.transaction.entity.Transaction;
import com.personalaccount.domain.transaction.entity.TransactionType;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {

    /**
     * 거래 생성
     * - 복식부기 자동 처리
     * - 대차평형 검증 포함
     */
    Transaction createTransaction(Long userId, TransactionCreateRequest request);

    /**
     * 거래 목록 조회
     * - 필터링: 타입(수입/지출), 기간
     */
    List<Transaction> getTransactions(
            Long userId,
            Long bookId,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * 거래 단건 조회
     */
    Transaction getTransaction(Long userId, Long id);

    /**
     * 거래 상세 조회 (복식부기 포함)
     * - Transaction + JournalEntry + TransactionDetail
     */
    TransactionDetailResponse getTransactionWithDetails(Long userId, Long id);

    /**
     * 거래 수정 (메모만)
     */
    Transaction updateTransaction(Long userId, Long id, TransactionUpdateRequest request);

    /**
     * 거래 삭제 (Soft Delete)
     */
    void deleteTransaction(Long userId, Long id);
}