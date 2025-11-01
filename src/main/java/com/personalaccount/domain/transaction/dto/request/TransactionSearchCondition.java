package com.personalaccount.domain.transaction.dto.request;

import com.personalaccount.domain.transaction.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 거래 검색 조건 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionSearchCondition {

    private Long bookId;              // 필수
    private TransactionType type;     // 선택 (null이면 전체)
    private LocalDate startDate;      // 선택
    private LocalDate endDate;        // 선택
    private String keyword;           // 선택 (메모 검색)
}