package com.personalaccount.domain.transaction.entity;

import com.personalaccount.domain.account.entity.Account;
import com.personalaccount.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "transaction_details",
        indexes = {
                @Index(name = "idx_transaction_detail_journal_entry_id", columnList = "journal_entry_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TransactionDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DetailType detailType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal debitAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal creditAmount;
}