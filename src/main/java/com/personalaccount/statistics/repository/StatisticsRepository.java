package com.personalaccount.statistics.repository;

import com.personalaccount.account.entity.AccountType;
import com.personalaccount.account.entity.QAccount;
import com.personalaccount.statistics.dto.CategoryStatistics;
import com.personalaccount.statistics.dto.MonthlySummary;
import com.personalaccount.transaction.entity.*;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class StatisticsRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 월별 요약 통계
     */
    public List<MonthlySummary> getMonthlySummary(Long bookId, LocalDate startDate) {
        QTransaction transaction = QTransaction.transaction;
        QJournalEntry journalEntry = QJournalEntry.journalEntry;
        QTransactionDetail detail = QTransactionDetail.transactionDetail;

        return queryFactory
                .select(Projections.constructor(
                        MonthlySummary.class,
                        Expressions.stringTemplate(
                                "TO_CHAR({0}, 'YYYY-MM')",
                                transaction.date
                        ),
                        new CaseBuilder()
                                .when(transaction.type.eq(TransactionType.INCOME))
                                .then(detail.debitAmount.add(detail.creditAmount))
                                .otherwise(BigDecimal.ZERO)
                                .sum(),
                        new CaseBuilder()
                                .when(transaction.type.eq(TransactionType.EXPENSE))
                                .then(detail.debitAmount.add(detail.creditAmount))
                                .otherwise(BigDecimal.ZERO)
                                .sum()
                ))
                .from(transaction)
                .join(journalEntry).on(journalEntry.transaction.eq(transaction))
                .join(detail).on(detail.journalEntry.eq(journalEntry))
                .where(
                        transaction.book.id.eq(bookId),
                        transaction.date.goe(startDate),
                        transaction.isActive.isTrue()
                )
                .groupBy(Expressions.stringTemplate(
                        "TO_CHAR({0}, 'YYYY-MM')",
                        transaction.date
                ))
                .orderBy(Expressions.stringTemplate(
                        "TO_CHAR({0}, 'YYYY-MM')",
                        transaction.date
                ).asc())
                .fetch();
    }

    /**
     * 수입 카테고리별 통계
     */
    public List<CategoryStatistics> getIncomeCategoryStatistics(
            Long bookId,
            YearMonth yearMonth
    ) {
        return getCategoryStatisticsByAccountType(
                bookId,
                yearMonth,
                TransactionType.INCOME,
                AccountType.REVENUE
        );
    }

    /**
     * 지출 카테고리별 통계
     */
    public List<CategoryStatistics> getExpenseCategoryStatistics(
            Long bookId,
            YearMonth yearMonth
    ) {
        return getCategoryStatisticsByAccountType(
                bookId,
                yearMonth,
                TransactionType.EXPENSE,
                AccountType.EXPENSE
        );
    }

    /**
     * 카테고리별 통계 공통 로직 (private)
     */
    private List<CategoryStatistics> getCategoryStatisticsByAccountType(
            Long bookId,
            YearMonth yearMonth,
            TransactionType transactionType,
            AccountType accountType
    ) {
        QTransaction transaction = QTransaction.transaction;
        QJournalEntry journalEntry = QJournalEntry.journalEntry;
        QTransactionDetail detail = QTransactionDetail.transactionDetail;
        QAccount account = QAccount.account;

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return queryFactory
                .select(Projections.constructor(
                        CategoryStatistics.class,
                        account.id,
                        account.code,
                        account.name,
                        detail.debitAmount.add(detail.creditAmount).sum()
                ))
                .from(transaction)
                .join(journalEntry).on(journalEntry.transaction.eq(transaction))
                .join(detail).on(detail.journalEntry.eq(journalEntry))
                .join(account).on(detail.account.eq(account))
                .where(
                        transaction.book.id.eq(bookId),
                        transaction.date.between(startDate, endDate),
                        transaction.type.eq(transactionType),
                        transaction.isActive.isTrue(),
                        account.accountType.eq(accountType)  // ← 파라미터로 받음
                )
                .groupBy(account.id, account.code, account.name)
                .orderBy(detail.debitAmount.add(detail.creditAmount).sum().desc())
                .fetch();
    }
}