package com.personalaccount.application.report.analysis.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AnalysisResponse {

    private AnalysisSummary summary;                    // 통계 요약
    private AiAnalysisComment aiAnalysis;               // AI 분석 코멘트
    private List<CategoryExpense> categoryExpenses;     // 카테고리별 지출
}