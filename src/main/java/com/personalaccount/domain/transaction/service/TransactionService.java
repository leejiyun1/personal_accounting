package com.personalaccount.domain.transaction.service;

import com.personalaccount.domain.transaction.dto.request.TransactionCreateRequest;
import com.personalaccount.domain.transaction.dto.request.TransactionUpdateRequest;
import com.personalaccount.domain.transaction.dto.response.TransactionWithAmountResponse;
import com.personalaccount.domain.transaction.entity.Transaction;
import com.personalaccount.domain.transaction.entity.TransactionType;

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

    List<TransactionWithAmountResponse> getTransactionsByAccountWithAmount(
            Long userId,
            Long bookId,
            Long accountId
    );
}