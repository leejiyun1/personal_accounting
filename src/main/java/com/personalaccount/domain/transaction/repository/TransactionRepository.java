package com.personalaccount.domain.transaction.repository;

import com.personalaccount.domain.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Transaction Repository
 * - 기본 CRUD (JpaRepository)
 * - 복잡한 검색 쿼리 (TransactionRepositoryCustom)
 */
@Repository
public interface TransactionRepository
        extends JpaRepository<Transaction, Long>, TransactionRepositoryCustom {

    /**
     * Soft Delete 조회
     */
    Optional<Transaction> findByIdAndIsActive(Long id, Boolean isActive);

    /**
     * 거래 상세 조회 (Book, User Fetch Join)
     * - N+1 문제 방지
     */
    @Query("SELECT t FROM Transaction t " +
            "JOIN FETCH t.book b " +
            "JOIN FETCH b.user " +
            "WHERE t.id = :id AND t.isActive = true")
    Optional<Transaction> findByIdWithBookAndUser(@Param("id") Long id);
}