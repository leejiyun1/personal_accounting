package com.personalaccount.domain.transaction.service;

import com.personalaccount.domain.transaction.dto.request.TransactionCreateRequest;
import com.personalaccount.domain.transaction.dto.request.TransactionUpdateRequest;
import com.personalaccount.domain.transaction.entity.Transaction;
import com.personalaccount.domain.transaction.entity.TransactionType;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {

    Transaction createTransaction(Long userId, TransactionCreateRequest request);

    List<Transaction> getTransactions(
            Long userId,
            Long bookId,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate
    );

    Transaction getTransaction(Long userId, Long id);

    Transaction updateTransaction(Long userId, Long id, TransactionUpdateRequest request);

    void deleteTransaction(Long userId, Long id);
}