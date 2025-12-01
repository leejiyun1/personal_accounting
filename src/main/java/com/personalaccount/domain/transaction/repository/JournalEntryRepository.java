package com.personalaccount.domain.transaction.repository;

import com.personalaccount.domain.transaction.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JournalEntry Repository
 * - 분개 기본 CRUD
 */
@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    /**
     * 특정 거래의 분개 목록 조회
     */
    List<JournalEntry> findByTransactionId(Long transactionId);
}