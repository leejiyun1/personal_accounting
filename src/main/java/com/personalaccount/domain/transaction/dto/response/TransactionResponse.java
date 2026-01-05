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

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "거래 기본 응답")
public class TransactionResponse {

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
}