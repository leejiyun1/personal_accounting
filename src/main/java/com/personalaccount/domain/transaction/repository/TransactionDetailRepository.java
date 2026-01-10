package com.personalaccount.domain.transaction.repository;

import com.personalaccount.domain.transaction.entity.TransactionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionDetailRepository extends JpaRepository<TransactionDetail, Long> {

    @Query("""
        select td
        from TransactionDetail td
        join fetch td.account
        where td.journalEntry.id = :journalEntryId
    """)
    List<TransactionDetail> findWithAccountByJournalEntryId(
            @Param("journalEntryId") Long journalEntryId
    );

    @Query("""
        select td
        from TransactionDetail td
        join fetch td.account
        where td.journalEntry.id in :journalEntryIds
    """)
    List<TransactionDetail> findWithAccountByJournalEntryIdIn(
            @Param("journalEntryIds") List<Long> journalEntryIds
    );
}
