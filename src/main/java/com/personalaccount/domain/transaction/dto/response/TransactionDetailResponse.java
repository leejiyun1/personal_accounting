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

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDetailResponse {
    private Long id;
    private Long bookId;
    private LocalDate date;
    private TransactionType type;
    private BigDecimal amount;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<JournalEntryInfo> journalEntries;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class JournalEntryInfo {
        private Long id;
        private String description;
        private List<DetailInfo> details;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailInfo {
        private Long id;
        private String accountCode;
        private String accountName;
        private String detailType;
        private BigDecimal debitAmount;
        private BigDecimal creditAmount;
    }
}