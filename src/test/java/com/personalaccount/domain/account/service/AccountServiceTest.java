package com.personalaccount.domain.account.service;

import com.personalaccount.domain.account.entity.Account;
import com.personalaccount.domain.account.entity.AccountType;
import com.personalaccount.domain.account.repository.AccountRepository;
import com.personalaccount.domain.account.service.impl.AccountServiceImpl;
import com.personalaccount.domain.book.entity.BookType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService 테스트")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    // 테스트 데이터
    private Account revenueAccount;
    private Account expenseAccount;
    private Account paymentAccount;

    @BeforeEach
    void setUp() {
        // 수입 카테고리 (수익 계정)
        revenueAccount = Account.builder()
                .id(1L)
                .code("5100")
                .name("급여")
                .accountType(AccountType.REVENUE)
                .bookType(BookType.PERSONAL)
                .isActive(true)
                .build();

        // 지출 카테고리 (비용 계정)
        expenseAccount = Account.builder()
                .id(3L)
                .code("6100")
                .name("식비")
                .accountType(AccountType.EXPENSE)
                .bookType(BookType.PERSONAL)
                .isActive(true)
                .build();

        // 결제수단 (자산 계정)
        paymentAccount = Account.builder()
                .id(2L)
                .code("1100")
                .name("보통예금")
                .accountType(AccountType.PAYMENT_METHOD)
                .bookType(BookType.PERSONAL)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("수입_카테고리_조회_성공")
    void getIncomeCategories_Success() {
        // Given: REVENUE 타입 계정 반환 Mock
        given(accountRepository.findByBookTypeAndAccountTypeAndIsActive(
                BookType.PERSONAL, AccountType.REVENUE, true))
                .willReturn(List.of(revenueAccount));

        // When: 수입 카테고리 조회
        List<Account> result = accountService.getIncomeCategories(BookType.PERSONAL);

        // Then: 결과 검증
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAccountType()).isEqualTo(AccountType.REVENUE);
        assertThat(result.get(0).getName()).isEqualTo("급여");
    }

    @Test
    @DisplayName("지출_카테고리_조회_성공")
    void getExpenseCategories_Success() {}

    @Test
    @DisplayName("결제수단_조회_성공")
    void getPaymentMethods_Success() {}

    @Test
    @DisplayName("전체_계정과목_조회_성공")
    void getAllAccounts_Success() {}

    @Test
    @DisplayName("ID로_계정과목_조회_성공")
    void getAccountById_Success() {}

    @Test
    @DisplayName("존재하지않는_ID_조회_예외발생")
    void getAccountById_NotFound_ThrowsException() {}

    @Test
    @DisplayName("비활성화_계정_필터링_검증")
    void getAccounts_FilterInactive() {}
}