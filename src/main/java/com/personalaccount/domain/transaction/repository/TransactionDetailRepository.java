package com.personalaccount.domain.transaction.repository;

import com.personalaccount.domain.transaction.entity.TransactionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TransactionDetail Repository
 * - 거래 상세(차변/대변) 기본 CRUD
 */
@Repository
public interface TransactionDetailRepository extends JpaRepository<TransactionDetail, Long> {

    /**
     * 특정 분개의 상세 내역 조회 (차변/대변)
     */
    List<TransactionDetail> findByJournalEntryId(Long journalEntryId);
}