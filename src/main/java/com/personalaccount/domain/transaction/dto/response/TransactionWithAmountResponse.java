package com.personalaccount.domain.transaction.dto.response;

import com.personalaccount.domain.transaction.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionWithAmountResponse {

    private Long id;
    private LocalDate date;
    private TransactionType type;
    private String memo;
    private BigDecimal amount;

    // QueryDSL/JPQL 생성자용
    public TransactionWithAmountResponse(
            Long id,
            LocalDate date,
            TransactionType type,
            String memo,
            BigDecimal debitAmount,
            BigDecimal creditAmount
    ) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.memo = memo;
        this.amount = debitAmount.add(creditAmount);
    }
}