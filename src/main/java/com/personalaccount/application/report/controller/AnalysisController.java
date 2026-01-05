package com.personalaccount.application.report.controller;

import com.personalaccount.application.report.service.ReportService;
import com.personalaccount.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final ReportService reportService;

    @GetMapping("/{bookId}")
    public CommonResponse<Map<String, Object>> getAnalysis(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long bookId,
            @RequestParam String yearMonth
    ) {
        log.info("GET /api/v1/analysis/{} - userId={}", bookId, userId);
        Map<String, Object> result = reportService.getAnalysis(userId, bookId, yearMonth);
        return CommonResponse.success(result);
    }
}