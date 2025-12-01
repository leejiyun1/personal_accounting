package com.personalaccount.domain.transaction.dto.response;

import com.personalaccount.domain.transaction.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 거래 상세 응답 DTO (복식부기 포함)
 * - Transaction + JournalEntry + TransactionDetail
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDetailResponse {

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

    /**
     * 분개 목록 (복식부기 상세)
     */
    private List<JournalEntryInfo> journalEntries;

    /**
     * 분개 정보
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class JournalEntryInfo {
        /**
         * 분개 ID
         */
        private Long id;

        /**
         * 분개 설명
         * - 예: "수입 - 급여 500000원"
         */
        private String description;

        /**
         * 차변/대변 상세 내역
         */
        private List<DetailInfo> details;
    }

    /**
     * 차변/대변 상세 정보
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailInfo {
        /**
         * 상세 내역 ID
         */
        private Long id;

        /**
         * 계정과목 코드
         * - 예: "1010", "5100"
         */
        private String accountCode;

        /**
         * 계정과목 이름
         * - 예: "보통예금", "급여"
         */
        private String accountName;

        /**
         * 차변/대변 구분 (DEBIT/CREDIT)
         */
        private String detailType;

        /**
         * 차변 금액
         */
        private BigDecimal debitAmount;

        /**
         * 대변 금액
         */
        private BigDecimal creditAmount;
    }
}