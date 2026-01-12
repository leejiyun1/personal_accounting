package com.personalaccount.presentation;

import com.personalaccount.application.report.dto.response.AccountBalance;
import com.personalaccount.application.report.dto.response.CategorySummary;
import com.personalaccount.application.report.dto.response.MonthlySummary;
import com.personalaccount.application.report.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatisticsController 테스트")
class StatisticsControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private StatisticsController statisticsController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(statisticsController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return 1L;
                    }
                })
                .build();
    }

    @Test
    @DisplayName("월별_요약_조회_성공")
    void getMonthlySummary_Success() throws Exception {
        MonthlySummary summary = MonthlySummary.builder()
                .yearMonth("2025-01")
                .income(new BigDecimal("300000"))
                .expense(new BigDecimal("100000"))
                .balance(new BigDecimal("200000"))
                .build();

        given(reportService.getMonthlySummary(1L, 1L)).willReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/statistics/monthly/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].yearMonth").value("2025-01"));

        verify(reportService).getMonthlySummary(1L, 1L);
    }

    @Test
    @DisplayName("카테고리별_통계_조회_성공")
    void getCategoryStatistics_Success() throws Exception {
        CategorySummary summary = CategorySummary.builder()
                .categoryName("식비")
                .amount(new BigDecimal("50000"))
                .percentage(30.0)
                .build();

        given(reportService.getCategoryStatistics(eq(1L), eq(1L), eq("2025-01"), eq("EXPENSE")))
                .willReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/statistics/category/1")
                        .param("yearMonth", "2025-01")
                        .param("type", "EXPENSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].categoryName").value("식비"));

        verify(reportService).getCategoryStatistics(eq(1L), eq(1L), eq("2025-01"), eq("EXPENSE"));
    }

    @Test
    @DisplayName("계정_잔액_조회_성공")
    void getAccountBalances_Success() throws Exception {
        AccountBalance balance = AccountBalance.builder()
                .accountId(1L)
                .accountName("보통예금")
                .balance(new BigDecimal("1000000"))
                .build();

        given(reportService.getAccountBalances(1L, 1L)).willReturn(List.of(balance));

        mockMvc.perform(get("/api/v1/statistics/balances/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].accountName").value("보통예금"));

        verify(reportService).getAccountBalances(1L, 1L);
    }
}