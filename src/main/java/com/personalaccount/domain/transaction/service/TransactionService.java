package com.personalaccount.domain.transaction.service;

import com.personalaccount.domain.transaction.dto.request.TransactionCreateRequest;
import com.personalaccount.domain.transaction.dto.request.TransactionUpdateRequest;
import com.personalaccount.domain.transaction.dto.response.TransactionDetailResponse;
import com.personalaccount.domain.transaction.dto.response.TransactionResponse;
import com.personalaccount.domain.transaction.entity.TransactionType;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {

    /**
     * 거래 생성 (복식부기 자동 처리)
     */
    TransactionResponse createTransaction(Long userId, TransactionCreateRequest request);

    List<TransactionResponse> getTransactions(
            Long userId,
            Long bookId,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate
    );

    TransactionResponse getTransaction(Long userId, Long id);

    TransactionDetailResponse getTransactionWithDetails(Long userId, Long id);

    TransactionResponse updateTransaction(Long userId, Long id, TransactionUpdateRequest request);

    void deleteTransaction(Long userId, Long id);
}