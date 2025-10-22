package com.personalaccount.transaction.service;

import com.personalaccount.transaction.dto.request.TransactionCreateRequest;
import com.personalaccount.transaction.dto.request.TransactionUpdateRequest;
import com.personalaccount.transaction.entity.Transaction;
import com.personalaccount.transaction.entity.TransactionType;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {

    Transaction createTransaction(Long userId, TransactionCreateRequest request);

    List<Transaction> getTransactions(Long userId, Long bookId);

    List<Transaction> getTransactionsByType(Long userId, Long bookId, TransactionType type);

    List<Transaction> getTransactionsByDateRange(
            Long userId,
            Long bookId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Transaction> getTransactionsByAccount(Long userId, Long bookId, Long accountId);

    Transaction getTransaction(Long userId, Long id);

    Transaction updateTransaction(Long userId, Long id, TransactionUpdateRequest request);

    void deleteTransaction(Long userId, Long id);
}