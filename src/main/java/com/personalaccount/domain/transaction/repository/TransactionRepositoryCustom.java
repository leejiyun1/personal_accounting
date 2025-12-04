package com.personalaccount.domain.transaction.repository;

import com.personalaccount.domain.transaction.dto.request.TransactionSearchCondition;
import com.personalaccount.domain.transaction.entity.Transaction;

import java.util.List;

public interface TransactionRepositoryCustom {
    List<Transaction> searchTransactions(TransactionSearchCondition condition);
}