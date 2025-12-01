package com.personalaccount.domain.transaction.dto.request;

import com.personalaccount.domain.transaction.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 거래 검색 조건 DTO
 * - QueryDSL 동적 쿼리에 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionSearchCondition {

    /**
     * 장부 ID (필수)
     */
    private Long bookId;

    /**
     * 거래 타입 (선택)
     * - null이면 전체 조회
     */
    private TransactionType type;

    /**
     * 시작 날짜 (선택)
     */
    private LocalDate startDate;

    /**
     * 종료 날짜 (선택)
     */
    private LocalDate endDate;

    /**
     * 메모 검색 키워드 (선택)
     * - 대소문자 구분 없음
     */
    private String keyword;
}