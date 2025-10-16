package com.personalaccount.transaction.entity;

import com.personalaccount.book.entity.Book;
import com.personalaccount.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "transactions")
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

    @Column(length = 500)
    private String memo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // === 비즈니스 메서드 ===

    public void updateMemo(String newMemo) {
        this.memo = newMemo;
    }

    public void deactivate() {
        this.isActive = false;
    }
}