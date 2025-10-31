package com.personalaccount.ledger.repository;

import com.personalaccount.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LedgerRepository extends JpaRepository<Transaction, Long> {

    // 자산 계산
    @Query("SELECT SUM(td.debitAmount - td.creditAmount) " +
            "FROM Transaction t " +
            "JOIN JournalEntry je ON je.transaction = t " +
            "JOIN TransactionDetail td ON td.journalEntry = je " +
            "JOIN Account a ON td.account = a " +
            "WHERE t.book.id = :bookId " +
            "AND t.date <= :asOfDate " +
            "AND t.isActive = true " +
            "AND a.accountType = 'ASSET'")
    BigDecimal findTotalAssets(
            @Param("bookId") Long bookId,
            @Param("asOfDate") LocalDate asOfDate
    );

    // 부채 계산
    @Query("SELECT SUM(td.creditAmount - td.debitAmount) " +
            "FROM Transaction t " +
            "JOIN JournalEntry je ON je.transaction = t " +
            "JOIN TransactionDetail td ON td.journalEntry = je " +
            "JOIN Account a ON td.account = a " +
            "WHERE t.book.id = :bookId " +
            "AND t.date <= :asOfDate " +
            "AND t.isActive = true " +
            "AND a.accountType = 'LIABILITY'")
    BigDecimal findTotalLiabilities(
            @Param("bookId") Long bookId,
            @Param("asOfDate") LocalDate asOfDate
    );

    // 기초 잔액 계산 (특정 날짜 이전까지의 누적)
    @Query("SELECT SUM(td.debitAmount - td.creditAmount) " +
            "FROM Transaction t " +
            "JOIN JournalEntry je ON je.transaction = t " +
            "JOIN TransactionDetail td ON td.journalEntry = je " +
            "WHERE t.book.id = :bookId " +
            "AND td.account.id = :accountId " +
            "AND t.date < :startDate " +
            "AND t.isActive = true")
    BigDecimal findOpeningBalance(
            @Param("bookId") Long bookId,
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDate startDate
    );

    // 계정별 거래 내역 조회 (날짜, 메모, 차변, 대변)
    @Query("SELECT t.date, t.memo, td.debitAmount, td.creditAmount " +
            "FROM Transaction t " +
            "JOIN JournalEntry je ON je.transaction = t " +
            "JOIN TransactionDetail td ON td.journalEntry = je " +
            "WHERE t.book.id = :bookId " +
            "AND td.account.id = :accountId " +
            "AND t.date BETWEEN :startDate AND :endDate " +
            "AND t.isActive = true " +
            "ORDER BY t.date ASC")
    List<Object[]> findAccountLedgerEntries(
            @Param("bookId") Long bookId,
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
