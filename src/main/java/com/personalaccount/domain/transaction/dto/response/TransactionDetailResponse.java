package com.personalaccount.domain.transaction.dto.response;

import com.personalaccount.domain.transaction.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "거래 상세 응답 (복식부기 포함)")
public class TransactionDetailResponse {

    @Schema(description = "거래 ID", example = "1")
    private Long id;

    @Schema(description = "장부 ID", example = "1")
    private Long bookId;

    @Schema(description = "거래 날짜", example = "2025-01-05")
    private LocalDate date;

    @Schema(description = "거래 타입 (INCOME: 수입, EXPENSE: 지출)", example = "INCOME")
    private TransactionType type;

    @Schema(description = "금액", example = "500000")
    private BigDecimal amount;

    @Schema(description = "메모", example = "월급날 급여 입금")
    private String memo;

    @Schema(description = "생성 일시", example = "2025-01-05T09:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2025-01-05T09:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "분개 목록 (복식부기)")
    private List<JournalEntryInfo> journalEntries;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "분개 정보")
    public static class JournalEntryInfo {

        @Schema(description = "분개 ID", example = "1")
        private Long id;

        @Schema(description = "분개 설명", example = "수입 - 급여 500000원")
        private String description;

        @Schema(description = "분개 상세 목록 (차변/대변)")
        private List<DetailInfo> details;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "분개 상세 정보 (차변/대변)")
    public static class DetailInfo {

        @Schema(description = "분개 상세 ID", example = "1")
        private Long id;

        @Schema(description = "계정과목 코드", example = "1200")
        private String accountCode;

        @Schema(description = "계정과목 이름", example = "보통예금")
        private String accountName;

        @Schema(description = "차변/대변 구분 (DEBIT: 차변, CREDIT: 대변)", example = "DEBIT")
        private String detailType;

        @Schema(description = "차변 금액", example = "500000")
        private BigDecimal debitAmount;

        @Schema(description = "대변 금액", example = "0")
        private BigDecimal creditAmount;
    }
}