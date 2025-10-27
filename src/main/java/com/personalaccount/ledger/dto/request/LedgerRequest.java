package com.personalaccount.ledger.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LedgerRequest {

    @NotNull(message = "장부 ID는 필수입니다")
    private Long bookId;

    @NotNull(message = "조회 월은 필수입니다")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "YYYY-MM 형식이어야 합니다")
    private String yearMonth;
}