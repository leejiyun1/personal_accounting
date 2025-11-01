package com.personalaccount.domain.transaction.repository.impl;

import com.personalaccount.domain.transaction.dto.request.TransactionSearchCondition;
import com.personalaccount.domain.transaction.entity.QTransaction;
import com.personalaccount.domain.transaction.entity.Transaction;
import com.personalaccount.domain.transaction.repository.TransactionRepositoryCustom;
import com.personalaccount.domain.transaction.repository.specification.TransactionSpecification;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Transaction Custom Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final TransactionSpecification spec;
    private static final QTransaction transaction = QTransaction.transaction;

    @Override
    public List<Transaction> searchTransactions(TransactionSearchCondition condition) {
        return queryFactory
                .selectFrom(transaction)
                .leftJoin(transaction.book).fetchJoin()
                .where(
                        spec.bookIdEq(condition.getBookId()),
                        spec.typeEq(condition.getType()),
                        spec.dateBetween(condition.getStartDate(), condition.getEndDate()),
                        spec.memoContains(condition.getKeyword()),
                        spec.isActive()
                )
                .orderBy(transaction.date.desc(), transaction.id.desc())
                .fetch();
    }
}