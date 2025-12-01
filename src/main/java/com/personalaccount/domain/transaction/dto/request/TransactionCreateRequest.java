package com.personalaccount.domain.transaction.dto.request;

import com.personalaccount.domain.transaction.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 거래 생성 요청 DTO
 * - Controller에서 @Valid로 자동 검증
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCreateRequest {

    /**
     * 장부 ID (필수)
     */
    @NotNull(message = "장부 ID는 필수입니다.")
    private Long bookId;

    /**
     * 거래 날짜 (필수)
     */
    @NotNull(message = "거래 날짜는 필수입니다.")
    private LocalDate date;

    /**
     * 거래 타입: INCOME(수입) / EXPENSE(지출) (필수)
     */
    @NotNull(message = "거래 타입은 필수입니다.")
    private TransactionType type;

    /**
     * 금액 (필수, 0보다 커야 함)
     */
    @NotNull(message = "금액은 필수입니다.")
    @DecimalMin(value = "0.01", message = "금액은 0보다 커야합니다.")
    private BigDecimal amount;

    /**
     * 카테고리(계정과목) ID (필수)
     * - 수입: REVENUE 타입
     * - 지출: EXPENSE 타입
     */
    @NotNull(message = "카테고리 ID는 필수입니다.")
    private Long categoryId;

    /**
     * 결제수단 ID (필수)
     * - PAYMENT_METHOD 타입
     */
    @NotNull(message = "결제수단 ID는 필수입니다.")
    private Long paymentMethodId;

    /**
     * 메모 (선택, 최대 500자)
     */
    @Size(max = 500, message = "메모는 500자를 초과할 수 없습니다.")
    private String memo;
}