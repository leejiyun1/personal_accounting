package com.personalaccount.domain.transaction.repository.specification;

import com.personalaccount.domain.transaction.entity.QTransaction;
import com.personalaccount.domain.transaction.entity.TransactionType;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Transaction 조회 조건 Specification
 *
 * Specification Pattern:
 * - 동적 쿼리 조건을 재사용 가능한 단위로 분리
 * - Optional 패턴으로 null-safe 처리
 * - 조건 조합의 유연성 확보
 */
@Component
public class TransactionSpecification {

    private static final QTransaction transaction = QTransaction.transaction;

    public BooleanExpression bookIdEq(Long bookId) {
        return Optional.ofNullable(bookId)
                .map(transaction.book.id::eq)
                .orElse(null);
    }

    public BooleanExpression typeEq(TransactionType type) {
        return Optional.ofNullable(type)
                .map(transaction.type::eq)
                .orElse(null);
    }

    public BooleanExpression dateBetween(LocalDate startDate, LocalDate endDate) {
        return Optional.ofNullable(startDate)
                .flatMap(start -> Optional.ofNullable(endDate)
                        .map(end -> transaction.date.between(start, end)))
                .orElse(null);
    }

    public BooleanExpression isActive() {
        return transaction.isActive.isTrue();
    }

    public BooleanExpression memoContains(String keyword) {
        return Optional.ofNullable(keyword)
                .filter(k -> !k.isBlank())
                .map(transaction.memo::containsIgnoreCase)
                .orElse(null);
    }
}