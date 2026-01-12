package com.personalaccount.application.report.controller;

import com.personalaccount.application.report.dto.response.*;
import com.personalaccount.application.report.service.ReportService;
import com.personalaccount.presentation.LedgerController;
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
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("LedgerController 테스트")
class LedgerControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private LedgerController ledgerController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ledgerController)
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
    @DisplayName("재무제표_조회_성공")
    void getFinancialStatement_Success() throws Exception {
        IncomeStatement incomeStatement = IncomeStatement.builder()
                .totalIncome(new BigDecimal("300000"))
                .totalExpense(new BigDecimal("100000"))
                .netProfit(new BigDecimal("200000"))
                .profitRate(66.67)
                .build();

        BalanceSheet balanceSheet = BalanceSheet.builder()
                .totalAssets(new BigDecimal("5000000"))
                .totalLiabilities(new BigDecimal("1000000"))
                .totalEquity(new BigDecimal("4000000"))
                .build();

        FinancialStatement statement = FinancialStatement.builder()
                .incomeStatement(incomeStatement)
                .balanceSheet(balanceSheet)
                .build();

        given(reportService.getFinancialStatement(eq(1L), eq(1L), eq("2025-01")))
                .willReturn(statement);

        mockMvc.perform(get("/api/v1/ledger/statement/1")
                        .param("yearMonth", "2025-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.incomeStatement.totalIncome").value(300000));

        verify(reportService).getFinancialStatement(eq(1L), eq(1L), eq("2025-01"));
    }

    @Test
    @DisplayName("계정별_원장_조회_성공")
    void getAccountLedger_Success() throws Exception {
        Map<String, Object> ledger = new HashMap<>();
        ledger.put("accountName", "보통예금");
        ledger.put("openingBalance", new BigDecimal("1000000"));
        ledger.put("closingBalance", new BigDecimal("1200000"));

        given(reportService.getAccountLedger(eq(1L), eq(1L), eq(1L), eq("2025-01")))
                .willReturn(ledger);

        mockMvc.perform(get("/api/v1/ledger/account/1/1")
                        .param("yearMonth", "2025-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountName").value("보통예금"));

        verify(reportService).getAccountLedger(eq(1L), eq(1L), eq(1L), eq("2025-01"));
    }
}