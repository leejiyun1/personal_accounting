package com.personalaccount.application.report.repository;

import com.personalaccount.domain.account.entity.AccountType;
import com.personalaccount.domain.account.entity.QAccount;
import com.personalaccount.domain.transaction.entity.*;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReportQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 수입 총액 (REVENUE 계정의 creditAmount)
     */
    public BigDecimal findTotalIncome(Long bookId, LocalDate startDate, LocalDate endDate) {
        QTransaction transaction = QTransaction.transaction;
        QJournalEntry journalEntry = QJournalEntry.journalEntry;
        QTransactionDetail detail = QTransactionDetail.transactionDetail;
        QAccount account = QAccount.account;

        BigDecimal result = queryFactory
                .select(detail.creditAmount.sum())
                .from(transaction)
                .join(journalEntry).on(journalEntry.transaction.eq(transaction))
                .join(detail).on(detail.journalEntry.eq(journalEntry))
                .join(account).on(detail.account.eq(account))
                .where(
                        transaction.book.id.eq(bookId),
                        transaction.date.between(startDate, endDate),
                        transaction.isActive.isTrue(),
                        account.accountType.eq(AccountType.REVENUE)
                )
                .fetchOne();

        return result != null ? result : BigDecimal.ZERO;
    }

    /**
     * 지출 총액 (EXPENSE 계정의 debitAmount)
     */
    public BigDecimal findTotalExpense(Long bookId, LocalDate startDate, LocalDate endDate) {
        QTransaction transaction = QTransaction.transaction;
        QJournalEntry journalEntry = QJournalEntry.journalEntry;
        QTransactionDetail detail = QTransactionDetail.transactionDetail;
        QAccount account = QAccount.account;

        BigDecimal result = queryFactory
                .select(detail.debitAmount.sum())
                .from(transaction)
                .join(journalEntry).on(journalEntry.transaction.eq(transaction))
                .join(detail).on(detail.journalEntry.eq(journalEntry))
                .join(account).on(detail.account.eq(account))
                .where(
                        transaction.book.id.eq(bookId),
                        transaction.date.between(startDate, endDate),
                        transaction.isActive.isTrue(),
                        account.accountType.eq(AccountType.EXPENSE)
                )
                .fetchOne();

        return result != null ? result : BigDecimal.ZERO;
    }

    /**
     * 카테고리별 지출 TOP5 (EXPENSE 계정의 debitAmount)
     */
    public List<Tuple> findCategoryExpenses(Long bookId, LocalDate startDate, LocalDate endDate) {
        QTransaction transaction = QTransaction.transaction;
        QJournalEntry journalEntry = QJournalEntry.journalEntry;
        QTransactionDetail detail = QTransactionDetail.transactionDetail;
        QAccount account = QAccount.account;

        return queryFactory
                .select(
                        account.name,
                        detail.debitAmount.sum()
                )
                .from(transaction)
                .join(journalEntry).on(journalEntry.transaction.eq(transaction))
                .join(detail).on(detail.journalEntry.eq(journalEntry))
                .join(account).on(detail.account.eq(account))
                .where(
                        transaction.book.id.eq(bookId),
                        transaction.date.between(startDate, endDate),
                        transaction.isActive.isTrue(),
                        account.accountType.eq(AccountType.EXPENSE)
                )
                .groupBy(account.name)
                .orderBy(detail.debitAmount.sum().desc())
                .fetch();
    }

    /**
     * 자산 총액 (ASSET 계정의 차변-대변)
     */
    public BigDecimal findTotalAssets(Long bookId, LocalDate asOfDate) {
        QTransaction transaction = QTransaction.transaction;
        QJournalEntry journalEntry = QJournalEntry.journalEntry;
        QTransactionDetail detail = QTransactionDetail.transactionDetail;
        QAccount account = QAccount.account;

        BigDecimal result = queryFactory
                .select(detail.debitAmount.subtract(detail.creditAmount).sum())
                .from(transaction)
                .join(journalEntry).on(journalEntry.transaction.eq(transaction))
                .join(detail).on(detail.journalEntry.eq(journalEntry))
                .join(account).on(detail.account.eq(account))
                .where(
                        transaction.book.id.eq(bookId),
                        transaction.date.loe(asOfDate),
                        transaction.isActive.isTrue(),
                        account.accountType.eq(AccountType.ASSET)
                )
                .fetchOne();

        return result != null ? result : BigDecimal.ZERO;
    }

    /**
     * 부채 총액 (LIABILITY 계정의 대변-차변)
     */
    public BigDecimal findTotalLiabilities(Long bookId, LocalDate asOfDate) {
        QTransaction transaction = QTransaction.transaction;
        QJournalEntry journalEntry = QJournalEntry.journalEntry;
        QTransactionDetail detail = QTransactionDetail.transactionDetail;
        QAccount account = QAccount.account;

        BigDecimal result = queryFactory
                .select(detail.creditAmount.subtract(detail.debitAmount).sum())
                .from(transaction)
                .join(journalEntry).on(journalEntry.transaction.eq(transaction))
                .join(detail).on(detail.journalEntry.eq(journalEntry))
                .join(account).on(detail.account.eq(account))
                .where(
                        transaction.book.id.eq(bookId),
                        transaction.date.loe(asOfDate),
                        transaction.isActive.isTrue(),
                        account.accountType.eq(AccountType.LIABILITY)
                )
                .fetchOne();

        return result != null ? result : BigDecimal.ZERO;
    }

    /**
     * 기초 잔액 (특정 계정의 startDate 이전 누적)
     */
    public BigDecimal findOpeningBalance(Long bookId, Long accountId, LocalDate startDate) {
        QTransaction transaction = QTransaction.transaction;
        QJournalEntry journalEntry = QJournalEntry.journalEntry;
        QTransactionDetail detail = QTransactionDetail.transactionDetail;

        BigDecimal result = queryFactory
                .select(detail.debitAmount.subtract(detail.creditAmount).sum())
                .from(transaction)
                .join(journalEntry).on(journalEntry.transaction.eq(transaction))
                .join(detail).on(detail.journalEntry.eq(journalEntry))
                .where(
                        transaction.book.id.eq(bookId),
                        detail.account.id.eq(accountId),
                        transaction.date.lt(startDate),
                        transaction.isActive.isTrue()
                )
                .fetchOne();

        return result != null ? result : BigDecimal.ZERO;
    }

    /**
     * 계정별 거래 내역 (날짜, 메모, 차변, 대변)
     */
    public List<Tuple> findAccountLedgerEntries(
            Long bookId,
            Long accountId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        QTransaction transaction = QTransaction.transaction;
        QJournalEntry journalEntry = QJournalEntry.journalEntry;
        QTransactionDetail detail = QTransactionDetail.transactionDetail;

        return queryFactory
                .select(
                        transaction.date,
                        transaction.memo,
                        detail.debitAmount,
                        detail.creditAmount
                )
                .from(transaction)
                .join(journalEntry).on(journalEntry.transaction.eq(transaction))
                .join(detail).on(detail.journalEntry.eq(journalEntry))
                .where(
                        transaction.book.id.eq(bookId),
                        detail.account.id.eq(accountId),
                        transaction.date.between(startDate, endDate),
                        transaction.isActive.isTrue()
                )
                .orderBy(transaction.date.asc())
                .fetch();
    }

    /**
     * 특정 계정의 현재 잔액 (차변 - 대변)
     */
    public BigDecimal findAccountBalance(Long bookId, Long accountId) {
        QTransaction transaction = QTransaction.transaction;
        QJournalEntry journalEntry = QJournalEntry.journalEntry;
        QTransactionDetail detail = QTransactionDetail.transactionDetail;

        BigDecimal result = queryFactory
                .select(detail.debitAmount.subtract(detail.creditAmount).sum())
                .from(transaction)
                .join(journalEntry).on(journalEntry.transaction.eq(transaction))
                .join(detail).on(detail.journalEntry.eq(journalEntry))
                .where(
                        transaction.book.id.eq(bookId),
                        detail.account.id.eq(accountId),
                        transaction.isActive.isTrue()
                )
                .fetchOne();

        return result != null ? result : BigDecimal.ZERO;
    }
}