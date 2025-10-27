package com.personalaccount.analysis.service;

import com.personalaccount.analysis.dto.response.AnalysisResponse;

public interface AnalysisService {

    /**
     * 경영 분석 조회
     * @param userId 사용자 ID
     * @param bookId 장부 ID
     * @param yearMonth 조회 월 (YYYY-MM)
     * @return 경영 분석 결과
     */
    AnalysisResponse getAnalysis(Long userId, Long bookId, String yearMonth);
}