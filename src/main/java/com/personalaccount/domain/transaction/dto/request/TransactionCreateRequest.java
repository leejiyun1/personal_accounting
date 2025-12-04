package com.personalaccount.domain.transaction.dto.request;

import com.personalaccount.domain.transaction.entity.TransactionType;
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
public class TransactionCreateRequest {

    @NotNull(message = "장부 ID는 필수입니다.")
    private Long bookId;

    @NotNull(message = "거래 날짜는 필수입니다.")
    private LocalDate date;

    @NotNull(message = "거래 타입은 필수입니다.")
    private TransactionType type;

    @NotNull(message = "금액은 필수입니다.")
    @DecimalMin(value = "0.01", message = "금액은 0보다 커야합니다.")
    private BigDecimal amount;

    @NotNull(message = "카테고리 ID는 필수입니다.")
    private Long categoryId;

    @NotNull(message = "결제수단 ID는 필수입니다.")
    private Long paymentMethodId;

    @Size(max = 500, message = "메모는 500자를 초과할 수 없습니다.")
    private String memo;
}