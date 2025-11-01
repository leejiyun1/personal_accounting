package com.personalaccount.domain.transaction.repository.specification;

import com.personalaccount.domain.transaction.entity.QTransaction;
import com.personalaccount.domain.transaction.entity.TransactionType;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Transaction 조회 조건 Specification
 *
 * Specification Pattern:
 * - 동적 쿼리 조건을 재사용 가능한 단위로 분리
 * - null-safe 처리 자동화
 * - 조건 조합의 유연성 확보
 */
@Component
public class TransactionSpecification {

    private static final QTransaction transaction = QTransaction.transaction;

    public BooleanExpression bookIdEq(Long bookId) {
        return bookId != null ? transaction.book.id.eq(bookId) : null;
    }

    public BooleanExpression typeEq(TransactionType type) {
        return type != null ? transaction.type.eq(type) : null;
    }

    public BooleanExpression dateBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return null;
        }
        return transaction.date.between(startDate, endDate);
    }

    public BooleanExpression isActive() {
        return transaction.isActive.isTrue();
    }

    public BooleanExpression memoContains(String keyword) {
        return keyword != null && !keyword.isBlank()
                ? transaction.memo.containsIgnoreCase(keyword)
                : null;
    }
}