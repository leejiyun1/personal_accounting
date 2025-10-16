package com.personalaccount.transaction.repository;

import com.personalaccount.transaction.entity.Transaction;
import com.personalaccount.transaction.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
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

    Optional<Transaction> findByIdAndIsActive(Long id, Boolean isActive);
}
