package com.personalaccount.domain.transaction.repository;

import com.personalaccount.domain.transaction.entity.TransactionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionDetailRepository extends JpaRepository<TransactionDetail, Long> {
    List<TransactionDetail> findByJournalEntryId(Long journalEntryId);

    /**
     * 여러 분개의 상세 내역을 한 번에 조회 (N+1 방지)
     */
    List<TransactionDetail> findByJournalEntryIdIn(List<Long> journalEntryIds);
}