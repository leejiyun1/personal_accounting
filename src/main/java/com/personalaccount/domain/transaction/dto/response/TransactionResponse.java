package com.personalaccount.domain.transaction.dto.response;

import com.personalaccount.domain.transaction.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 거래 단건 응답 DTO
 * - 복식부기 상세 없음
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    /**
     * 거래 ID
     */
    private Long id;

    /**
     * 장부 ID
     */
    private Long bookId;

    /**
     * 거래 날짜
     */
    private LocalDate date;

    /**
     * 거래 타입 (INCOME/EXPENSE)
     */
    private TransactionType type;

    /**
     * 금액
     */
    private BigDecimal amount;

    /**
     * 메모
     */
    private String memo;

    /**
     * 생성 일시
     */
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     */
    private LocalDateTime updatedAt;
}