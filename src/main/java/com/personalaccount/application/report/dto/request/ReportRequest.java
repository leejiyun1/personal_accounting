package com.personalaccount.application.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "리포트 조회 요청")
public class ReportRequest {

    @Schema(
            description = "장부 ID",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "장부 ID는 필수입니다")
    private Long bookId;

    @Schema(
            description = "조회 월 (YYYY-MM 형식)",
            example = "2025-01",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "조회 월은 필수입니다")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "YYYY-MM 형식이어야 합니다")
    private String yearMonth;
}