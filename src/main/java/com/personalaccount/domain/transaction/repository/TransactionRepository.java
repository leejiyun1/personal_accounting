package com.personalaccount.domain.transaction.repository;

import com.personalaccount.domain.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository
        extends JpaRepository<Transaction, Long>, TransactionRepositoryCustom {

    Optional<Transaction> findByIdAndIsActive(Long id, Boolean isActive);

    @Query("SELECT t FROM Transaction t " +
            "JOIN FETCH t.book b " +
            "JOIN FETCH b.user " +
            "WHERE t.id = :id AND t.isActive = true")
    Optional<Transaction> findByIdWithBookAndUser(@Param("id") Long id);
}