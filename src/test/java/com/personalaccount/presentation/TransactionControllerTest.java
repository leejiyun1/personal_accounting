package com.personalaccount.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.personalaccount.domain.transaction.dto.request.TransactionCreateRequest;
import com.personalaccount.domain.transaction.dto.request.TransactionUpdateRequest;
import com.personalaccount.domain.transaction.dto.response.TransactionResponse;
import com.personalaccount.domain.transaction.entity.TransactionType;
import com.personalaccount.domain.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionController 테스트")
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController)
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

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("거래_생성_성공")
    void createTransaction_Success() throws Exception {
        TransactionCreateRequest request = TransactionCreateRequest.builder()
                .bookId(1L)
                .date(LocalDate.now())
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100000"))
                .categoryId(1L)
                .paymentMethodId(2L)
                .memo("월급")
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .id(1L)
                .bookId(1L)
                .date(LocalDate.now())
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100000"))
                .memo("월급")
                .build();

        given(transactionService.createTransaction(eq(1L), any(TransactionCreateRequest.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.amount").value(100000));

        verify(transactionService).createTransaction(eq(1L), any(TransactionCreateRequest.class));
    }

    @Test
    @DisplayName("거래_목록_조회_성공")
    void getTransactions_Success() throws Exception {
        TransactionResponse response = TransactionResponse.builder()
                .id(1L)
                .bookId(1L)
                .date(LocalDate.now())
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100000"))
                .memo("월급")
                .build();

        given(transactionService.getTransactions(
                eq(1L), eq(1L), eq(TransactionType.INCOME), any(), any()))
                .willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/transactions")
                        .param("bookId", "1")
                        .param("type", "INCOME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(transactionService).getTransactions(
                eq(1L), eq(1L), eq(TransactionType.INCOME), any(), any());
    }

    @Test
    @DisplayName("거래_단건_조회_성공")
    void getTransaction_Success() throws Exception {
        TransactionResponse response = TransactionResponse.builder()
                .id(1L)
                .bookId(1L)
                .date(LocalDate.now())
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100000"))
                .memo("월급")
                .build();

        given(transactionService.getTransaction(1L, 1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(transactionService).getTransaction(1L, 1L);
    }

    @Test
    @DisplayName("거래_수정_성공")
    void updateTransaction_Success() throws Exception {
        TransactionUpdateRequest request = TransactionUpdateRequest.builder()
                .memo("수정된메모")
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .id(1L)
                .bookId(1L)
                .date(LocalDate.now())
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100000"))
                .memo("수정된메모")
                .build();

        given(transactionService.updateTransaction(eq(1L), eq(1L), any(TransactionUpdateRequest.class)))
                .willReturn(response);

        mockMvc.perform(put("/api/v1/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.memo").value("수정된메모"));

        verify(transactionService).updateTransaction(eq(1L), eq(1L), any(TransactionUpdateRequest.class));
    }

    @Test
    @DisplayName("거래_삭제_성공")
    void deleteTransaction_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(transactionService).deleteTransaction(1L, 1L);
    }
}