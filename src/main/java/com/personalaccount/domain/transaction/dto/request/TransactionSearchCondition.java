package com.personalaccount.domain.transaction.dto.request;

import com.personalaccount.domain.transaction.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionSearchCondition {
    private Long bookId;
    private TransactionType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String keyword;
}