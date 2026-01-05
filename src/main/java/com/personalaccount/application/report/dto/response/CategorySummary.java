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
@Schema(description = "카테고리별 통계")
public class CategorySummary {

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "카테고리 코드", example = "5100")
    private String categoryCode;

    @Schema(description = "카테고리 이름", example = "식비")
    private String categoryName;

    @Schema(description = "금액", example = "500000")
    private BigDecimal amount;

    @Schema(description = "비율 (%)", example = "25.5")
    private Double percentage;
}