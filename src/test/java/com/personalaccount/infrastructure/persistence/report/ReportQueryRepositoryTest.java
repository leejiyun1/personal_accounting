package com.personalaccount.infrastructure.persistence.report;

import com.personalaccount.domain.account.entity.Account;
import com.personalaccount.domain.account.entity.AccountType;
import com.personalaccount.domain.account.repository.AccountRepository;
import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.entity.BookType;
import com.personalaccount.domain.book.repository.BookRepository;
import com.personalaccount.domain.transaction.entity.*;
import com.personalaccount.domain.transaction.repository.JournalEntryRepository;
import com.personalaccount.domain.transaction.repository.TransactionDetailRepository;
import com.personalaccount.domain.transaction.repository.TransactionRepository;
import com.personalaccount.domain.user.entity.User;
import com.personalaccount.domain.user.repository.UserRepository;
import com.querydsl.core.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ReportQueryRepository 통합 테스트")
class ReportQueryRepositoryTest {

    @Autowired private ReportQueryRepository reportQueryRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private JournalEntryRepository journalEntryRepository;
    @Autowired private TransactionDetailRepository transactionDetailRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;

    private Book testBook;
    private Account revenueAccount;
    private Account expenseAccount;
    private Account paymentMethodAccount;
    private Account assetAccount;
    private Account liabilityAccount;

    @BeforeEach
    void setUp() {
        User testUser = userRepository.save(User.builder()
                .email("test@test.com")
                .password("password")
                .name("테스터")
                .isActive(true)
                .build());

        testBook = bookRepository.save(Book.builder()
                .user(testUser)
                .name("테스트장부")
                .bookType(BookType.PERSONAL)
                .isActive(true)
                .build());

        revenueAccount = accountRepository.save(Account.builder()
                .code("5100")
                .name("급여")
                .accountType(AccountType.REVENUE)
                .bookType(BookType.PERSONAL)
                .isActive(true)
                .build());

        expenseAccount = accountRepository.save(Account.builder()
                .code("6100")
                .name("식비")
                .accountType(AccountType.EXPENSE)
                .bookType(BookType.PERSONAL)
                .isActive(true)
                .build());

        paymentMethodAccount = accountRepository.save(Account.builder()
                .code("1010")
                .name("보통예금")
                .accountType(AccountType.PAYMENT_METHOD)
                .bookType(BookType.PERSONAL)
                .isActive(true)
                .build());

        assetAccount = accountRepository.save(Account.builder()
                .code("1100")
                .name("현금")
                .accountType(AccountType.ASSET)
                .bookType(BookType.PERSONAL)
                .isActive(true)
                .build());

        liabilityAccount = accountRepository.save(Account.builder()
                .code("2100")
                .name("외상매입금")
                .accountType(AccountType.LIABILITY)
                .bookType(BookType.PERSONAL)
                .isActive(true)
                .build());

        setupTestTransactions();
    }

    private void setupTestTransactions() {
        LocalDate today = LocalDate.now();
        createIncomeTransaction(new BigDecimal("300000"), today);
        createExpenseTransaction(new BigDecimal("50000"), today);
        createAssetIncreaseTransaction(new BigDecimal("100000"), today);
        createLiabilityTransaction(new BigDecimal("30000"), today);
    }

    private void createIncomeTransaction(BigDecimal amount, LocalDate date) {
        Transaction transaction = transactionRepository.save(Transaction.builder()
                .book(testBook)
                .date(date)
                .type(TransactionType.INCOME)
                .amount(amount)
                .memo("급여 입금")
                .isActive(true)
                .build());

        JournalEntry entry = journalEntryRepository.save(JournalEntry.builder()
                .transaction(transaction)
                .date(date)
                .description("급여 수입")
                .build());

        List<TransactionDetail> details = new ArrayList<>();
        details.add(TransactionDetail.builder()
                .journalEntry(entry)
                .account(paymentMethodAccount)
                .detailType(DetailType.DEBIT)
                .debitAmount(amount)
                .creditAmount(BigDecimal.ZERO)
                .build());
        details.add(TransactionDetail.builder()
                .journalEntry(entry)
                .account(revenueAccount)
                .detailType(DetailType.CREDIT)
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(amount)
                .build());

        transactionDetailRepository.saveAll(details);
    }

    private void createExpenseTransaction(BigDecimal amount, LocalDate date) {
        Transaction transaction = transactionRepository.save(Transaction.builder()
                .book(testBook)
                .date(date)
                .type(TransactionType.EXPENSE)
                .amount(amount)
                .memo("점심 식대")
                .isActive(true)
                .build());

        JournalEntry entry = journalEntryRepository.save(JournalEntry.builder()
                .transaction(transaction)
                .date(date)
                .description("식비 지출")
                .build());

        List<TransactionDetail> details = new ArrayList<>();
        details.add(TransactionDetail.builder()
                .journalEntry(entry)
                .account(expenseAccount)
                .detailType(DetailType.DEBIT)
                .debitAmount(amount)
                .creditAmount(BigDecimal.ZERO)
                .build());
        details.add(TransactionDetail.builder()
                .journalEntry(entry)
                .account(paymentMethodAccount)
                .detailType(DetailType.CREDIT)
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(amount)
                .build());

        transactionDetailRepository.saveAll(details);
    }

    private void createAssetIncreaseTransaction(BigDecimal amount, LocalDate date) {
        Transaction transaction = transactionRepository.save(Transaction.builder()
                .book(testBook)
                .date(date)
                .type(TransactionType.INCOME)
                .amount(amount)
                .memo("현금 수령")
                .isActive(true)
                .build());

        JournalEntry entry = journalEntryRepository.save(JournalEntry.builder()
                .transaction(transaction)
                .date(date)
                .description("현금 자산 증가")
                .build());

        List<TransactionDetail> details = new ArrayList<>();
        details.add(TransactionDetail.builder()
                .journalEntry(entry)
                .account(assetAccount)
                .detailType(DetailType.DEBIT)
                .debitAmount(amount)
                .creditAmount(BigDecimal.ZERO)
                .build());
        details.add(TransactionDetail.builder()
                .journalEntry(entry)
                .account(revenueAccount)
                .detailType(DetailType.CREDIT)
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(amount)
                .build());

        transactionDetailRepository.saveAll(details);
    }

    private void createLiabilityTransaction(BigDecimal amount, LocalDate date) {
        Transaction transaction = transactionRepository.save(Transaction.builder()
                .book(testBook)
                .date(date)
                .type(TransactionType.EXPENSE)
                .amount(amount)
                .memo("외상 구매")
                .isActive(true)
                .build());

        JournalEntry entry = journalEntryRepository.save(JournalEntry.builder()
                .transaction(transaction)
                .date(date)
                .description("외상매입금 발생")
                .build());

        List<TransactionDetail> details = new ArrayList<>();
        details.add(TransactionDetail.builder()
                .journalEntry(entry)
                .account(expenseAccount)
                .detailType(DetailType.DEBIT)
                .debitAmount(amount)
                .creditAmount(BigDecimal.ZERO)
                .build());
        details.add(TransactionDetail.builder()
                .journalEntry(entry)
                .account(liabilityAccount)
                .detailType(DetailType.CREDIT)
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(amount)
                .build());

        transactionDetailRepository.saveAll(details);
    }

    @Test
    @DisplayName("수입_총액_조회")
    void findTotalIncome_AccuracyTest() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        BigDecimal totalIncome = reportQueryRepository.findTotalIncome(testBook.getId(), startDate, endDate);

        assertThat(totalIncome).isEqualByComparingTo(new BigDecimal("400000"));
    }

    @Test
    @DisplayName("지출_총액_조회")
    void findTotalExpense_AccuracyTest() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        BigDecimal totalExpense = reportQueryRepository.findTotalExpense(testBook.getId(), startDate, endDate);

        assertThat(totalExpense).isEqualByComparingTo(new BigDecimal("80000"));
    }

    @Test
    @DisplayName("카테고리별_지출_집계")
    void findCategoryExpenses_AccuracyTest() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        List<Tuple> result = reportQueryRepository.findCategoryExpenses(testBook.getId(), startDate, endDate);

        assertThat(result).hasSize(1);

        Tuple expenseData = result.getFirst();
        assertThat(expenseData.get(0, String.class)).isEqualTo("식비");
        assertThat(expenseData.get(1, BigDecimal.class)).isEqualByComparingTo(new BigDecimal("80000"));
    }

    @Test
    @DisplayName("자산_총액_조회")
    void findTotalAssets_AccuracyTest() {
        LocalDate asOfDate = LocalDate.now();

        BigDecimal totalAssets = reportQueryRepository.findTotalAssets(testBook.getId(), asOfDate);

        assertThat(totalAssets).isEqualByComparingTo(new BigDecimal("100000"));
    }

    @Test
    @DisplayName("부채_총액_조회")
    void findTotalLiabilities_AccuracyTest() {
        LocalDate asOfDate = LocalDate.now();

        BigDecimal totalLiabilities = reportQueryRepository.findTotalLiabilities(testBook.getId(), asOfDate);

        assertThat(totalLiabilities).isEqualByComparingTo(new BigDecimal("30000"));
    }

    @Test
    @DisplayName("계정별_거래내역_조회")
    void findAccountLedgerEntries_AccuracyTest() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        List<Tuple> result = reportQueryRepository.findAccountLedgerEntries(
                testBook.getId(), paymentMethodAccount.getId(), startDate, endDate);

        assertThat(result).hasSize(2);

        Tuple firstEntry = result.get(0);
        assertThat(firstEntry.get(2, BigDecimal.class)).isEqualByComparingTo(new BigDecimal("300000"));

        Tuple secondEntry = result.get(1);
        assertThat(secondEntry.get(3, BigDecimal.class)).isEqualByComparingTo(new BigDecimal("50000"));
    }

    @Test
    @DisplayName("기초잔액_조회")
    void findOpeningBalance_AccuracyTest() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        createIncomeTransaction(new BigDecimal("150000"), yesterday);

        LocalDate startDate = LocalDate.now();

        BigDecimal openingBalance = reportQueryRepository.findOpeningBalance(
                testBook.getId(), paymentMethodAccount.getId(), startDate);

        assertThat(openingBalance).isEqualByComparingTo(new BigDecimal("150000"));
    }

    @Test
    @DisplayName("계정_현재잔액_조회")
    void findAccountBalance_AccuracyTest() {
        BigDecimal balance = reportQueryRepository.findAccountBalance(
                testBook.getId(), paymentMethodAccount.getId());

        assertThat(balance).isEqualByComparingTo(new BigDecimal("250000"));
    }

    @Test
    @DisplayName("기간별_수입지출_조회")
    void findIncomeExpenseByDateRange_AccuracyTest() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        List<Tuple> result = reportQueryRepository.findIncomeExpenseByDateRange(
                testBook.getId(), startDate, endDate);

        assertThat(result).isNotEmpty();

        long revenueCount = result.stream()
                .filter(t -> t.get(1, AccountType.class) == AccountType.REVENUE)
                .count();
        assertThat(revenueCount).isEqualTo(2);

        long expenseCount = result.stream()
                .filter(t -> t.get(1, AccountType.class) == AccountType.EXPENSE)
                .count();
        assertThat(expenseCount).isEqualTo(2);
    }

    @Test
    @DisplayName("계정_잔액_일괄조회")
    void findAccountBalancesByIds_AccuracyTest() {
        Map<Long, BigDecimal> balances = reportQueryRepository.findAccountBalancesByIds(
                testBook.getId(),
                List.of(paymentMethodAccount.getId(), assetAccount.getId())
        );

        assertThat(balances).hasSize(2);
        assertThat(balances.get(paymentMethodAccount.getId())).isEqualByComparingTo(new BigDecimal("250000"));
        assertThat(balances.get(assetAccount.getId())).isEqualByComparingTo(new BigDecimal("100000"));
    }

    @Test
    @DisplayName("계정_잔액_일괄조회_빈리스트")
    void findAccountBalancesByIds_EmptyList_ReturnsEmptyMap() {
        Map<Long, BigDecimal> result = reportQueryRepository.findAccountBalancesByIds(
                testBook.getId(), List.of());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("계정_잔액_일괄조회_존재하지않는계정")
    void findAccountBalancesByIds_NonExistentAccount_ReturnsZero() {
        Map<Long, BigDecimal> result = reportQueryRepository.findAccountBalancesByIds(
                testBook.getId(), List.of(999L));

        assertThat(result).containsKey(999L);
        assertThat(result.get(999L)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("데이터_없는_기간_조회")
    void findTotalIncome_NoData_ReturnsZero() {
        LocalDate futureStart = LocalDate.now().plusMonths(1);
        LocalDate futureEnd = futureStart.plusMonths(1);

        BigDecimal totalIncome = reportQueryRepository.findTotalIncome(
                testBook.getId(), futureStart, futureEnd);

        assertThat(totalIncome).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("존재하지않는_장부_조회")
    void findCategoryExpenses_NonExistentBook_ReturnsEmpty() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        List<Tuple> result = reportQueryRepository.findCategoryExpenses(
                999L, startDate, endDate);

        assertThat(result).isEmpty();
    }
}