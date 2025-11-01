package com.personalaccount.domain.transaction.dto.response;

import com.personalaccount.domain.transaction.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long id;
    private Long bookId;
    private LocalDate date;
    private TransactionType type;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
