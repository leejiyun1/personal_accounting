package com.personalaccount.application.report.analysis.service.impl;

import com.personalaccount.application.report.analysis.dto.response.AiAnalysisComment;
import com.personalaccount.application.report.analysis.dto.response.AnalysisResponse;
import com.personalaccount.application.report.analysis.dto.response.AnalysisSummary;
import com.personalaccount.application.report.analysis.dto.response.CategoryExpense;
import com.personalaccount.application.report.analysis.service.AnalysisService;
import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.repository.BookRepository;
import com.personalaccount.common.exception.custom.BookNotFoundException;
import com.personalaccount.common.exception.custom.UnauthorizedBookAccessException;
import com.personalaccount.domain.transaction.entity.TransactionType;
import com.personalaccount.domain.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.personalaccount.application.ai.client.AiClient;
import com.personalaccount.application.ai.dto.request.GeminiRequest;
import com.personalaccount.application.ai.dto.response.GeminiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisServiceImpl implements AnalysisService {

    private final BookRepository bookRepository;
    private final TransactionRepository transactionRepository;
    private final AiClient aiClient;

    @Override
    public AnalysisResponse getAnalysis(Long userId, Long bookId, String yearMonth) {
        log.info("경영 분석 조회: userId={}, bookId={}, yearMonth={}", userId, bookId, yearMonth);

        // 1. 장부 권한 확인
        validateBookAccess(userId, bookId);

        // 2. 날짜 계산
        LocalDate startDate = LocalDate.parse(yearMonth + "-01");
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        // 3. 통계 계산
        AnalysisSummary summary = calculateSummary(bookId, startDate, endDate);
        List<CategoryExpense> categoryExpenses = calculateCategoryExpenses(bookId, startDate, endDate);

        // 4. AI 분석
        AiAnalysisComment aiAnalysis = generateAiAnalysis(summary, categoryExpenses);

        return AnalysisResponse.builder()
                .summary(summary)
                .aiAnalysis(aiAnalysis)
                .categoryExpenses(categoryExpenses)
                .build();
    }

    /**
     * 통계 요약 계산
     */
    private AnalysisSummary calculateSummary(
            Long bookId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        BigDecimal totalIncome = transactionRepository
                .findTotalAmountByType(bookId, startDate, endDate, TransactionType.INCOME);

        BigDecimal totalExpense = transactionRepository
                .findTotalAmountByType(bookId, startDate, endDate, TransactionType.EXPENSE);

        // null 체크
        totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        totalExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;

        BigDecimal netProfit = totalIncome.subtract(totalExpense);

        Double profitRate = totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.divide(totalIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        return AnalysisSummary.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netProfit(netProfit)
                .profitRate(profitRate)
                .build();
    }

    /**
     * 카테고리별 지출 계산
     */
    private List<CategoryExpense> calculateCategoryExpenses(
            Long bookId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<Object[]> results = transactionRepository
                .findCategoryExpensesByBookIdAndDateRange(bookId, startDate, endDate);

        // 총 지출 계산
        BigDecimal totalExpense = results.stream()
                .map(row -> (BigDecimal) row[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // CategoryExpense로 변환
        return results.stream()
                .map(row -> {
                    String categoryName = (String) row[0];
                    BigDecimal amount = (BigDecimal) row[1];

                    Double percentage = totalExpense.compareTo(BigDecimal.ZERO) > 0
                            ? amount.divide(totalExpense, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue()
                            : 0.0;

                    return CategoryExpense.builder()
                            .categoryName(categoryName)
                            .amount(amount)
                            .percentage(percentage)
                            .build();
                })
                .limit(5)  // TOP 5
                .collect(Collectors.toList());
    }

    /**
     * AI 분석 생성
     */
    private AiAnalysisComment generateAiAnalysis(
            AnalysisSummary summary,
            List<CategoryExpense> categoryExpenses
    ) {
        // 1. 프롬프트 생성
        String prompt = buildAnalysisPrompt(summary, categoryExpenses);

        // 2. Gemini 호출
        GeminiRequest request = GeminiRequest.builder()
                .contents(List.of(
                        GeminiRequest.Content.builder()
                                .parts(List.of(
                                        GeminiRequest.Part.builder()
                                                .text(prompt)
                                                .build()
                                ))
                                .build()
                ))
                .build();

        GeminiResponse response = aiClient.sendMessage(request);
        String aiResponse = response.getCandidates().get(0)
                .getContent()
                .getParts().get(0)
                .getText();

        // 3. AI 응답 파싱
        return parseAiResponse(aiResponse);
    }

    /**
     * AI 프롬프트 생성
     */
    private String buildAnalysisPrompt(
            AnalysisSummary summary,
            List<CategoryExpense> categoryExpenses
    ) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("다음 재무 데이터를 분석하여 JSON 형식으로 응답해주세요.\n\n");
        prompt.append("### 재무 요약\n");
        prompt.append("- 총 수입: ").append(summary.getTotalIncome()).append("원\n");
        prompt.append("- 총 지출: ").append(summary.getTotalExpense()).append("원\n");
        prompt.append("- 순이익: ").append(summary.getNetProfit()).append("원\n");
        prompt.append("- 수익률: ").append(summary.getProfitRate()).append("%\n\n");

        prompt.append("### 카테고리별 지출 TOP 5\n");
        for (CategoryExpense expense : categoryExpenses) {
            prompt.append("- ").append(expense.getCategoryName())
                    .append(": ").append(expense.getAmount())
                    .append("원 (").append(expense.getPercentage()).append("%)\n");
        }

        prompt.append("\n### 응답 형식 (반드시 이 형식으로만 응답)\n");
        prompt.append("{\n");
        prompt.append("  \"overview\": \"전체 재무 상태 한 줄 요약\",\n");
        prompt.append("  \"strengths\": [\"잘한 점1\", \"잘한 점2\"],\n");
        prompt.append("  \"warnings\": [\"경고1\", \"경고2\"],\n");
        prompt.append("  \"suggestions\": [\"제안1\", \"제안2\"]\n");
        prompt.append("}\n");

        return prompt.toString();
    }

    /**
     * AI 응답 파싱
     */
    private AiAnalysisComment parseAiResponse(String aiResponse) {
        try {
            // JSON 추출 (```json ... ``` 제거)
            String jsonString = aiResponse;
            if (aiResponse.contains("```json")) {
                jsonString = aiResponse.substring(
                        aiResponse.indexOf("```json") + 7,
                        aiResponse.lastIndexOf("```")
                ).trim();
            } else if (aiResponse.contains("```")) {
                jsonString = aiResponse.substring(
                        aiResponse.indexOf("```") + 3,
                        aiResponse.lastIndexOf("```")
                ).trim();
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);

            return AiAnalysisComment.builder()
                    .overview(jsonNode.get("overview").asText())
                    .strengths(parseStringList(jsonNode.get("strengths")))
                    .warnings(parseStringList(jsonNode.get("warnings")))
                    .suggestions(parseStringList(jsonNode.get("suggestions")))
                    .build();

        } catch (Exception e) {
            log.error("AI 응답 파싱 실패: {}", aiResponse, e);
            // 파싱 실패 시 기본 응답
            return AiAnalysisComment.builder()
                    .overview("재무 분석을 생성할 수 없습니다.")
                    .strengths(List.of())
                    .warnings(List.of())
                    .suggestions(List.of())
                    .build();
        }
    }

    private List<String> parseStringList(JsonNode node) {
        List<String> result = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                result.add(item.asText());
            }
        }
        return result;
    }
    /**
     * 장부 접근 권한 확인
     */
    private void validateBookAccess(Long userId, Long bookId) {
        Book book = bookRepository.findByIdAndIsActive(bookId, true)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        if (!book.getUser().getId().equals(userId)) {
            throw new UnauthorizedBookAccessException(bookId);
        }
    }
}