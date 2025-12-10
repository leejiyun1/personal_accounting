package com.personalaccount.domain.transaction.entity;

import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_transaction_book_date", columnList = "book_id, date, is_active"),
                @Index(name = "idx_transaction_book_type", columnList = "book_id, type, is_active")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 500)
    private String memo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public void deactivate() {
        this.isActive = false;
    }
}