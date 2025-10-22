package com.personalaccount.transaction.repository;

import com.personalaccount.transaction.entity.Transaction;
import com.personalaccount.transaction.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByBookIdAndIsActiveOrderByDateDesc(
            Long bookId,
            Boolean isActive
    );

    List<Transaction> findByBookIdAndTypeAndIsActiveOrderByDateDesc(
            Long bookId,
            TransactionType type,
            Boolean isActive
    );

    List<Transaction> findByBookIdAndDateBetweenAndIsActiveOrderByDateDesc(
            Long bookId,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isActive
    );

    @Query("SELECT t FROM Transaction t " +
            "JOIN FETCH t.book b " +
            "JOIN FETCH b.user " +
            "WHERE t.book.id = :bookId AND t.isActive = :isActive " +
            "ORDER BY t.date DESC")
    List<Transaction> findByBookIdAndIsActiveWithBook(
            @Param("bookId") Long bookId,
            @Param("isActive") Boolean isActive
    );

    @Query("SELECT t FROM Transaction t " +
            "JOIN FETCH t.book b " +
            "JOIN FETCH b.user " +
            "WHERE t.book.id = :bookId AND t.type = :type AND t.isActive = :isActive " +
            "ORDER BY t.date DESC")
    List<Transaction> findByBookIdAndTypeAndIsActiveWithBook(
            @Param("bookId") Long bookId,
            @Param("type") TransactionType type,
            @Param("isActive") Boolean isActive
    );

    @Query("SELECT t FROM Transaction t " +
            "JOIN FETCH t.book b " +
            "JOIN FETCH b.user " +
            "WHERE t.book.id = :bookId " +
            "AND t.date BETWEEN :startDate AND :endDate " +
            "AND t.isActive = :isActive " +
            "ORDER BY t.date DESC")
    List<Transaction> findByBookIdAndDateBetweenAndIsActiveWithBook(
            @Param("bookId") Long bookId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("isActive") Boolean isActive
    );

    Optional<Transaction> findByIdAndIsActive(Long id, Boolean isActive);

    @Query("SELECT DISTINCT t FROM Transaction t " +
            "JOIN FETCH t.book b " +
            "JOIN FETCH b.user " +
            "JOIN JournalEntry je ON je.transaction = t " +
            "JOIN TransactionDetail td ON td.journalEntry = je " +
            "WHERE t.book.id = :bookId " +
            "AND td.account.id = :accountId " +
            "AND t.isActive = :isActive " +
            "ORDER BY t.date DESC")
    List<Transaction> findByBookIdAndAccountIdAndIsActive(
            @Param("bookId") Long bookId,
            @Param("accountId") Long accountId,
            @Param("isActive") Boolean isActive
    );
}