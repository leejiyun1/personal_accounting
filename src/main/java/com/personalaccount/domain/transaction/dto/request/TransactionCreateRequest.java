package com.personalaccount.domain.transaction.dto.request;

import com.personalaccount.domain.transaction.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "거래 생성 요청")
public class TransactionCreateRequest {

    @Schema(
            description = "장부 ID",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "장부 ID는 필수입니다.")
    private Long bookId;

    @Schema(
            description = "거래 날짜",
            example = "2025-01-05",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "거래 날짜는 필수입니다.")
    private LocalDate date;

    @Schema(
            description = "거래 타입 (INCOME: 수입, EXPENSE: 지출)",
            example = "INCOME",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "거래 타입은 필수입니다.")
    private TransactionType type;

    @Schema(
            description = "금액 (0보다 커야 함)",
            example = "500000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "금액은 필수입니다.")
    @DecimalMin(value = "0.01", message = "금액은 0보다 커야합니다.")
    private BigDecimal amount;

    @Schema(
            description = "카테고리 ID (수입: 급여/용돈 등, 지출: 식비/교통비 등)",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "카테고리 ID는 필수입니다.")
    private Long categoryId;

    @Schema(
            description = "결제수단 ID (현금/은행/카드 등)",
            example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "결제수단 ID는 필수입니다.")
    private Long paymentMethodId;

    @Schema(
            description = "메모 (선택적)",
            example = "월급날 급여 입금"
    )
    @Size(max = 500, message = "메모는 500자를 초과할 수 없습니다.")
    private String memo;
}