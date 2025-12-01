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
 * - null-safe 처리 자동화 (null 반환 시 조건 제외)
 * - 조건 조합의 유연성 확보
 */
@Component
public class TransactionSpecification {

    private static final QTransaction transaction = QTransaction.transaction;

    /**
     * 장부 ID 일치
     */
    public BooleanExpression bookIdEq(Long bookId) {
        return bookId != null ? transaction.book.id.eq(bookId) : null;
    }

    /**
     * 거래 타입 일치 (INCOME/EXPENSE)
     */
    public BooleanExpression typeEq(TransactionType type) {
        return type != null ? transaction.type.eq(type) : null;
    }

    /**
     * 날짜 범위 검색
     */
    public BooleanExpression dateBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return null;
        }
        return transaction.date.between(startDate, endDate);
    }

    /**
     * 활성화된 거래만 조회
     */
    public BooleanExpression isActive() {
        return transaction.isActive.isTrue();
    }

    /**
     * 메모 키워드 검색 (대소문자 무시)
     */
    public BooleanExpression memoContains(String keyword) {
        return keyword != null && !keyword.isBlank()
                ? transaction.memo.containsIgnoreCase(keyword)
                : null;
    }
}