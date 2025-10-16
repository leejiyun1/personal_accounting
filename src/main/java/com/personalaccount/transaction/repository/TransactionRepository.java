package com.personalaccount.transaction.repository;

import com.personalaccount.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByBookIdAndIsActiveOrderByDateDesc(
            Long bookId,
            Boolean usActive
    );

    List<Transaction> findByBookIdAndTypeAndIsActiveOrderByDateDesc(
            Long bookId,
            Transaction type,
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
