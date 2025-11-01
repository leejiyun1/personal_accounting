package com.personalaccount.application.report.analysis.controller;

import com.personalaccount.application.report.analysis.dto.response.AnalysisResponse;
import com.personalaccount.application.report.analysis.service.AnalysisService;
import com.personalaccount.common.dto.CommonResponse;
import com.personalaccount.common.dto.ResponseFactory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @GetMapping
    public ResponseEntity<CommonResponse<AnalysisResponse>> getAnalysis(
            @AuthenticationPrincipal Long userId,
            @RequestParam @NotNull Long bookId,
            @RequestParam @NotNull @Pattern(regexp = "\\d{4}-\\d{2}") String yearMonth
    ) {
        log.info("경영 분석 API 호출: userId={}, bookId={}, yearMonth={}", userId, bookId, yearMonth);

        AnalysisResponse response = analysisService.getAnalysis(userId, bookId, yearMonth);

        return ResponseEntity.ok(
                ResponseFactory.success(response)
        );
    }
}
