package com.personalaccount.application.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "AI 분석 코멘트")
public class AiAnalysisComment {

    @Schema(description = "전체 분석 요약", example = "이번 달 수익률이 전월 대비 10% 상승했습니다.")
    private String overview;

    @Schema(description = "잘한 점", example = "[\"식비 절감 성공\", \"고정비 관리 우수\"]")
    private List<String> strengths;

    @Schema(description = "경고 사항", example = "[\"교통비 급증\", \"비정기 지출 증가\"]")
    private List<String> warnings;

    @Schema(description = "개선 제안", example = "[\"대중교통 이용 권장\", \"예산 계획 수립 필요\"]")
    private List<String> suggestions;
}