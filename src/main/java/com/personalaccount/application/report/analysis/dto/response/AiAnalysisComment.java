package com.personalaccount.application.report.analysis.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AiAnalysisComment {

    private String overview;           // 전체 분석 요약
    private List<String> strengths;    // 잘한 점
    private List<String> warnings;     // 경고 사항
    private List<String> suggestions;  // 개선 제안
}