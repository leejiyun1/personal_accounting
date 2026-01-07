package com.personalaccount.domain.account.controller;

import com.personalaccount.domain.account.dto.mapper.AccountMapper;
import com.personalaccount.domain.account.dto.response.AccountResponse;
import com.personalaccount.domain.account.dto.response.CategoryResponse;
import com.personalaccount.domain.account.entity.Account;
import com.personalaccount.domain.account.entity.AccountType;
import com.personalaccount.domain.account.service.AccountService;
import com.personalaccount.domain.book.entity.BookType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountController 테스트")
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountController accountController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
    }

    @Test
    @DisplayName("수입_카테고리_조회_성공")
    void getIncomeCategories_Success() throws Exception {
        Account incomeAccount = Account.builder()
                .id(1L)
                .code("5100")
                .name("급여")
                .accountType(AccountType.REVENUE)
                .bookType(BookType.PERSONAL)
                .build();

        CategoryResponse response = CategoryResponse.builder()
                .id(1L)
                .code("5100")
                .name("급여")
                .build();

        given(accountService.getIncomeCategories(BookType.PERSONAL)).willReturn(List.of(incomeAccount));
        given(accountMapper.toCategoryResponse(incomeAccount)).willReturn(response);

        mockMvc.perform(get("/api/v1/categories/income")
                        .param("bookType", "PERSONAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("급여"));

        verify(accountService).getIncomeCategories(BookType.PERSONAL);
    }

    @Test
    @DisplayName("지출_카테고리_조회_성공")
    void getExpenseCategories_Success() throws Exception {
        Account expenseAccount = Account.builder()
                .id(2L)
                .code("6100")
                .name("식비")
                .accountType(AccountType.EXPENSE)
                .bookType(BookType.PERSONAL)
                .build();

        CategoryResponse response = CategoryResponse.builder()
                .id(2L)
                .code("6100")
                .name("식비")
                .build();

        given(accountService.getExpenseCategories(BookType.PERSONAL)).willReturn(List.of(expenseAccount));
        given(accountMapper.toCategoryResponse(expenseAccount)).willReturn(response);

        mockMvc.perform(get("/api/v1/categories/expense")
                        .param("bookType", "PERSONAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("식비"));

        verify(accountService).getExpenseCategories(BookType.PERSONAL);
    }

    @Test
    @DisplayName("결제수단_조회_성공")
    void getPaymentMethods_Success() throws Exception {
        Account paymentAccount = Account.builder()
                .id(3L)
                .code("1010")
                .name("보통예금")
                .accountType(AccountType.PAYMENT_METHOD)
                .bookType(BookType.PERSONAL)
                .build();

        CategoryResponse response = CategoryResponse.builder()
                .id(3L)
                .code("1010")
                .name("보통예금")
                .build();

        given(accountService.getPaymentMethods(BookType.PERSONAL)).willReturn(List.of(paymentAccount));
        given(accountMapper.toCategoryResponse(paymentAccount)).willReturn(response);

        mockMvc.perform(get("/api/v1/categories/payment-methods")
                        .param("bookType", "PERSONAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("보통예금"));

        verify(accountService).getPaymentMethods(BookType.PERSONAL);
    }

    @Test
    @DisplayName("전체_계정과목_조회_성공")
    void getAllAccounts_Success() throws Exception {
        Account incomeAccount = Account.builder()
                .id(1L)
                .code("5100")
                .name("급여")
                .accountType(AccountType.REVENUE)
                .bookType(BookType.PERSONAL)
                .build();

        AccountResponse response = AccountResponse.builder()
                .id(1L)
                .code("5100")
                .name("급여")
                .accountType(AccountType.REVENUE)
                .bookType(BookType.PERSONAL)
                .build();

        given(accountService.getAllAccounts(BookType.PERSONAL)).willReturn(List.of(incomeAccount));
        given(accountMapper.toAccountResponse(incomeAccount)).willReturn(response);

        mockMvc.perform(get("/api/v1/accounts")
                        .param("bookType", "PERSONAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].code").value("5100"));

        verify(accountService).getAllAccounts(BookType.PERSONAL);
    }

    @Test
    @DisplayName("계정과목_상세_조회_성공")
    void getAccount_Success() throws Exception {
        Account incomeAccount = Account.builder()
                .id(1L)
                .code("5100")
                .name("급여")
                .accountType(AccountType.REVENUE)
                .bookType(BookType.PERSONAL)
                .build();

        AccountResponse response = AccountResponse.builder()
                .id(1L)
                .code("5100")
                .name("급여")
                .accountType(AccountType.REVENUE)
                .bookType(BookType.PERSONAL)
                .build();

        given(accountService.getAccountById(1L)).willReturn(incomeAccount);
        given(accountMapper.toAccountResponse(incomeAccount)).willReturn(response);

        mockMvc.perform(get("/api/v1/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(accountService).getAccountById(1L);
    }
}