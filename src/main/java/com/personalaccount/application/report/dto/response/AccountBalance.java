package com.personalaccount.application.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "계정별 잔액")
public class AccountBalance {

    @Schema(description = "계정과목 ID", example = "1")
    private Long accountId;

    @Schema(description = "계정과목 이름", example = "보통예금")
    private String accountName;

    @Schema(description = "잔액", example = "2500000")
    private BigDecimal balance;
}