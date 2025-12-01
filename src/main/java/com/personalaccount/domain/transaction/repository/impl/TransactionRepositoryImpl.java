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
 * - QueryDSL 사용
 * - 동적 쿼리 처리
 */
@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final TransactionSpecification spec;
    private static final QTransaction transaction = QTransaction.transaction;

    /**
     * 동적 조건으로 거래 검색
     * - Fetch Join으로 N+1 방지
     * - null 조건은 자동 제외
     * - 최신순 정렬 (날짜 DESC, ID DESC)
     */
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