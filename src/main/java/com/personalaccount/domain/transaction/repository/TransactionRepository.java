package com.personalaccount.domain.transaction.repository;

import com.personalaccount.domain.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository
        extends JpaRepository<Transaction, Long>, TransactionRepositoryCustom {

    Optional<Transaction> findByIdAndIsActive(Long id, Boolean isActive);
}