package com.personalaccount.application.report.service;

import com.personalaccount.application.report.dto.response.*;
import com.personalaccount.infrastructure.persistence.report.ReportQueryRepository;
import com.personalaccount.application.report.service.impl.ReportServiceImpl;
import com.personalaccount.common.exception.custom.AccountNotFoundException;
import com.personalaccount.common.exception.custom.BookNotFoundException;
import com.personalaccount.common.exception.custom.UnauthorizedBookAccessException;
import com.personalaccount.domain.account.entity.Account;
import com.personalaccount.domain.account.entity.AccountType;
import com.personalaccount.domain.account.repository.AccountRepository;
import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.entity.BookType;
import com.personalaccount.domain.book.repository.BookRepository;
import com.personalaccount.domain.user.entity.User;
import com.querydsl.core.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService 테스트")
class ReportServiceImplTest {

    @Mock
    private ReportQueryRepository reportQueryRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private User testUser;
    private Book testBook;
    private Account paymentAccount;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("테스터")
                .build();

        testBook = Book.builder()
                .id(1L)
                .user(testUser)
                .name("테스트장부")
                .bookType(BookType.PERSONAL)
                .isActive(true)
                .build();

        paymentAccount = Account.builder()
                .id(10L)
                .code("1010")
                .name("보통예금")
                .accountType(AccountType.PAYMENT_METHOD)
                .bookType(BookType.PERSONAL)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("월별_요약_조회_성공")
    void getMonthlySummary_Success() {
        given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));

        LocalDate now = LocalDate.now();
        List<Tuple> mockResults = createMockIncomeExpenseTuples(now);
        given(reportQueryRepository.findIncomeExpenseByDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(mockResults);

        List<MonthlySummary> result = reportService.getMonthlySummary(1L, 1L);

        assertThat(result).hasSize(6);

        MonthlySummary currentMonth = result.get(5);
        assertThat(currentMonth.getIncome()).isEqualByComparingTo(new BigDecimal("300000"));
        assertThat(currentMonth.getExpense()).isEqualByComparingTo(new BigDecimal("150000"));
        assertThat(currentMonth.getBalance()).isEqualByComparingTo(new BigDecimal("150000"));
    }

    @Test
    @DisplayName("월별_요약_조회_권한없음_예외발생")
    void getMonthlySummary_UnauthorizedAccess_ThrowsException() {
        User anotherUser = User.builder()
                .id(2L)
                .email("another@test.com")
                .name("다른사용자")
                .build();

        Book anotherBook = Book.builder()
                .id(1L)
                .user(anotherUser)
                .name("다른장부")
                .build();

        given(bookRepository.findById(1L)).willReturn(Optional.of(anotherBook));

        assertThatThrownBy(() -> reportService.getMonthlySummary(1L, 1L))
                .isInstanceOf(UnauthorizedBookAccessException.class);
    }

    @Test
    @DisplayName("월별_요약_조회_장부없음_예외발생")
    void getMonthlySummary_BookNotFound_ThrowsException() {
        given(bookRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.getMonthlySummary(1L, 999L))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    @DisplayName("카테고리별_통계_조회_성공")
    void getCategoryStatistics_Success() {
        given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));

        List<Tuple> mockResults = createMockCategoryExpenses();
        given(reportQueryRepository.findCategoryExpenses(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(mockResults);

        List<CategorySummary> result = reportService.getCategoryStatistics(1L, 1L, "2025-01", "EXPENSE");

        assertThat(result).hasSize(3);

        CategorySummary food = result.getFirst();
        assertThat(food.getCategoryName()).isEqualTo("식비");
        assertThat(food.getAmount()).isEqualByComparingTo(new BigDecimal("150000"));
        assertThat(food.getPercentage()).isCloseTo(50.0, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    @DisplayName("카테고리별_통계_조회_데이터없음_빈리스트반환")
    void getCategoryStatistics_NoData_ReturnsEmpty() {
        given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
        given(reportQueryRepository.findCategoryExpenses(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(Collections.emptyList());

        List<CategorySummary> result = reportService.getCategoryStatistics(1L, 1L, "2025-01", "EXPENSE");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("계정별_잔액_조회_성공")
    void getAccountBalances_Success() {
        given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));

        List<Account> mockAccounts = Arrays.asList(
                paymentAccount,
                Account.builder()
                        .id(11L)
                        .code("1011")
                        .name("신한카드")
                        .accountType(AccountType.PAYMENT_METHOD)
                        .bookType(BookType.PERSONAL)
                        .isActive(true)
                        .build()
        );

        given(accountRepository.findByBookTypeAndAccountTypeAndIsActive(
                BookType.PERSONAL, AccountType.PAYMENT_METHOD, true))
                .willReturn(mockAccounts);

        Map<Long, BigDecimal> mockBalances = new HashMap<>();
        mockBalances.put(10L, new BigDecimal("500000"));
        mockBalances.put(11L, new BigDecimal("200000"));

        given(reportQueryRepository.findAccountBalancesByIds(eq(1L), anyList()))
                .willReturn(mockBalances);

        List<AccountBalance> result = reportService.getAccountBalances(1L, 1L);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getAccountName()).isEqualTo("보통예금");
        assertThat(result.getFirst().getBalance()).isEqualByComparingTo(new BigDecimal("500000"));
    }

    @Test
    @DisplayName("계정별_잔액_조회_계정없음_빈리스트반환")
    void getAccountBalances_NoAccounts_ReturnsEmpty() {
        given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
        given(accountRepository.findByBookTypeAndAccountTypeAndIsActive(
                BookType.PERSONAL, AccountType.PAYMENT_METHOD, true))
                .willReturn(Collections.emptyList());

        List<AccountBalance> result = reportService.getAccountBalances(1L, 1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("재무제표_조회_성공")
    void getFinancialStatement_Success() {
        given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));

        given(reportQueryRepository.findTotalIncome(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(new BigDecimal("500000"));
        given(reportQueryRepository.findTotalExpense(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(new BigDecimal("300000"));
        given(reportQueryRepository.findTotalAssets(eq(1L), any(LocalDate.class)))
                .willReturn(new BigDecimal("1000000"));
        given(reportQueryRepository.findTotalLiabilities(eq(1L), any(LocalDate.class)))
                .willReturn(new BigDecimal("200000"));

        FinancialStatement result = reportService.getFinancialStatement(1L, 1L, "2025-01");

        IncomeStatement incomeStatement = result.getIncomeStatement();
        assertThat(incomeStatement.getTotalIncome()).isEqualByComparingTo(new BigDecimal("500000"));
        assertThat(incomeStatement.getTotalExpense()).isEqualByComparingTo(new BigDecimal("300000"));
        assertThat(incomeStatement.getNetProfit()).isEqualByComparingTo(new BigDecimal("200000"));
        assertThat(incomeStatement.getProfitRate()).isCloseTo(40.0, org.assertj.core.data.Offset.offset(0.01));

        BalanceSheet balanceSheet = result.getBalanceSheet();
        assertThat(balanceSheet.getTotalAssets()).isEqualByComparingTo(new BigDecimal("1000000"));
        assertThat(balanceSheet.getTotalLiabilities()).isEqualByComparingTo(new BigDecimal("200000"));
        assertThat(balanceSheet.getTotalEquity()).isEqualByComparingTo(new BigDecimal("800000"));
    }

    @Test
    @DisplayName("재무제표_조회_수입0_수익률0반환")
    void getFinancialStatement_ZeroIncome_ProfitRateZero() {
        given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
        given(reportQueryRepository.findTotalIncome(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(BigDecimal.ZERO);
        given(reportQueryRepository.findTotalExpense(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(new BigDecimal("100000"));
        given(reportQueryRepository.findTotalAssets(eq(1L), any(LocalDate.class)))
                .willReturn(BigDecimal.ZERO);
        given(reportQueryRepository.findTotalLiabilities(eq(1L), any(LocalDate.class)))
                .willReturn(BigDecimal.ZERO);

        FinancialStatement result = reportService.getFinancialStatement(1L, 1L, "2025-01");

        assertThat(result.getIncomeStatement().getProfitRate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("계정원장_조회_성공")
    void getAccountLedger_Success() {
        given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
        given(accountRepository.findById(10L)).willReturn(Optional.of(paymentAccount));

        given(reportQueryRepository.findOpeningBalance(eq(1L), eq(10L), any(LocalDate.class)))
                .willReturn(new BigDecimal("100000"));

        List<Tuple> mockEntries = createMockLedgerEntries();
        given(reportQueryRepository.findAccountLedgerEntries(eq(1L), eq(10L), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(mockEntries);

        Map<String, Object> result = reportService.getAccountLedger(1L, 1L, 10L, "2025-01");

        assertThat(result.get("accountName")).isEqualTo("보통예금");
        assertThat(result.get("openingBalance")).isEqualTo(new BigDecimal("100000"));

        @SuppressWarnings("unchecked")
        List<TransactionEntry> entries = (List<TransactionEntry>) result.get("entries");
        assertThat(entries).hasSize(2);

        TransactionEntry entry1 = entries.getFirst();
        assertThat(entry1.getDebit()).isEqualByComparingTo(new BigDecimal("300000"));
        assertThat(entry1.getBalance()).isEqualByComparingTo(new BigDecimal("400000"));

        assertThat(result.get("closingBalance")).isEqualTo(new BigDecimal("350000"));
    }

    @Test
    @DisplayName("계정원장_조회_계정없음_예외발생")
    void getAccountLedger_AccountNotFound_ThrowsException() {
        given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));
        given(reportQueryRepository.findOpeningBalance(eq(1L), eq(999L), any(LocalDate.class)))
                .willReturn(BigDecimal.ZERO);
        given(reportQueryRepository.findAccountLedgerEntries(eq(1L), eq(999L), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(Collections.emptyList());
        given(accountRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.getAccountLedger(1L, 1L, 999L, "2025-01"))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("AI_분석_조회_성공")
    void getAnalysis_Success() {
        given(bookRepository.findById(1L)).willReturn(Optional.of(testBook));

        given(reportQueryRepository.findTotalIncome(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(new BigDecimal("500000"));
        given(reportQueryRepository.findTotalExpense(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(new BigDecimal("300000"));
        given(reportQueryRepository.findTotalAssets(eq(1L), any(LocalDate.class)))
                .willReturn(new BigDecimal("1000000"));
        given(reportQueryRepository.findTotalLiabilities(eq(1L), any(LocalDate.class)))
                .willReturn(new BigDecimal("200000"));

        List<Tuple> mockCategories = createMockCategoryExpenses();
        given(reportQueryRepository.findCategoryExpenses(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(mockCategories);

        Map<String, Object> result = reportService.getAnalysis(1L, 1L, "2025-01");

        assertThat(result).containsKeys("summary", "topExpenses", "aiComment");

        FinancialStatement summary = (FinancialStatement) result.get("summary");
        assertThat(summary.getIncomeStatement().getNetProfit())
                .isEqualByComparingTo(new BigDecimal("200000"));

        @SuppressWarnings("unchecked")
        List<CategorySummary> topExpenses = (List<CategorySummary>) result.get("topExpenses");
        assertThat(topExpenses).hasSizeLessThanOrEqualTo(5);

        AiAnalysisComment aiComment = (AiAnalysisComment) result.get("aiComment");
        assertThat(aiComment.getOverview()).isEqualTo("분석 준비 중");
    }

    private List<Tuple> createMockIncomeExpenseTuples(LocalDate baseDate) {
        List<Tuple> tuples = new ArrayList<>();

        tuples.add(createMockTuple(
                baseDate,
                AccountType.REVENUE,
                new BigDecimal("300000"),
                BigDecimal.ZERO
        ));

        tuples.add(createMockTuple(
                baseDate,
                AccountType.EXPENSE,
                BigDecimal.ZERO,
                new BigDecimal("150000")
        ));

        return tuples;
    }

    private List<Tuple> createMockCategoryExpenses() {
        List<Tuple> tuples = new ArrayList<>();
        tuples.add(createMockCategoryTuple("식비", new BigDecimal("150000")));
        tuples.add(createMockCategoryTuple("교통비", new BigDecimal("100000")));
        tuples.add(createMockCategoryTuple("통신비", new BigDecimal("50000")));
        return tuples;
    }

    private List<Tuple> createMockLedgerEntries() {
        List<Tuple> tuples = new ArrayList<>();
        tuples.add(createMockLedgerTuple(
                LocalDate.now(),
                "급여 입금",
                new BigDecimal("300000"),
                BigDecimal.ZERO
        ));
        tuples.add(createMockLedgerTuple(
                LocalDate.now(),
                "점심 식대",
                BigDecimal.ZERO,
                new BigDecimal("50000")
        ));
        return tuples;
    }

    private Tuple createMockTuple(LocalDate date, AccountType accountType,
                                  BigDecimal creditAmount, BigDecimal debitAmount) {
        Tuple tuple = mock(Tuple.class);
        when(tuple.get(0, LocalDate.class)).thenReturn(date);
        when(tuple.get(1, AccountType.class)).thenReturn(accountType);
        when(tuple.get(2, BigDecimal.class)).thenReturn(creditAmount);
        when(tuple.get(3, BigDecimal.class)).thenReturn(debitAmount);
        return tuple;
    }

    private Tuple createMockCategoryTuple(String categoryName, BigDecimal amount) {
        Tuple tuple = mock(Tuple.class);
        when(tuple.get(0, String.class)).thenReturn(categoryName);
        when(tuple.get(1, BigDecimal.class)).thenReturn(amount);
        return tuple;
    }

    private Tuple createMockLedgerTuple(LocalDate date, String memo,
                                        BigDecimal debit, BigDecimal credit) {
        Tuple tuple = mock(Tuple.class);
        when(tuple.get(0, LocalDate.class)).thenReturn(date);
        when(tuple.get(1, String.class)).thenReturn(memo);
        when(tuple.get(2, BigDecimal.class)).thenReturn(debit);
        when(tuple.get(3, BigDecimal.class)).thenReturn(credit);
        return tuple;
    }
}