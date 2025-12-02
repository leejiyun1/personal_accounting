package com.personalaccount.domain.transaction.service;

import com.personalaccount.common.exception.custom.AccountNotFoundException;
import com.personalaccount.common.exception.custom.BookNotFoundException;
import com.personalaccount.common.exception.custom.InvalidTransactionException;
import com.personalaccount.common.exception.custom.UnauthorizedBookAccessException;
import com.personalaccount.domain.account.entity.Account;
import com.personalaccount.domain.account.entity.AccountType;
import com.personalaccount.domain.account.repository.AccountRepository;
import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.entity.BookType;
import com.personalaccount.domain.book.repository.BookRepository;
import com.personalaccount.domain.transaction.dto.request.TransactionCreateRequest;
import com.personalaccount.domain.transaction.entity.TransactionType;
import com.personalaccount.domain.transaction.repository.JournalEntryRepository;
import com.personalaccount.domain.transaction.repository.TransactionDetailRepository;
import com.personalaccount.domain.transaction.repository.TransactionRepository;
import com.personalaccount.domain.transaction.service.impl.TransactionServiceImpl;
import com.personalaccount.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService 예외 테스트")
class TransactionServiceExceptionTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private TransactionDetailRepository transactionDetailRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User testUser;
    private Book testBook;
    private Account revenueAccount;
    private Account expenseAccount;
    private Account paymentAccount;

    @BeforeEach
    void setUp() {
        // 테스트 사용자
        testUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("테스트유저")
                .build();

        // 테스트 장부
        testBook = Book.builder()
                .id(1L)
                .user(testUser)
                .bookType(BookType.PERSONAL)
                .name("개인장부")
                .build();

        // 수익 계정 (급여)
        revenueAccount = Account.builder()
                .id(1L)
                .code("5100")
                .name("급여")
                .accountType(AccountType.REVENUE)
                .bookType(BookType.PERSONAL)
                .build();

        // 비용 계정 (식비)
        expenseAccount = Account.builder()
                .id(3L)
                .code("6100")
                .name("식비")
                .accountType(AccountType.EXPENSE)
                .bookType(BookType.PERSONAL)
                .build();

        // 결제수단 계정 (보통예금)
        paymentAccount = Account.builder()
                .id(2L)
                .code("1010")
                .name("보통예금")
                .accountType(AccountType.PAYMENT_METHOD)
                .bookType(BookType.PERSONAL)
                .build();
    }

    // ========== 예외 테스트 ==========

    @Test
    @DisplayName("존재하지않는_장부_예외발생")
    void createTransaction_BookNotFound_ThrowsException() {
        // Given: 존재하지 않는 bookId
        TransactionCreateRequest request = TransactionCreateRequest.builder()
                .bookId(999L)
                .date(LocalDate.now())
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100000"))
                .categoryId(1L)
                .paymentMethodId(2L)
                .build();

        // Mock: empty 반환
        given(bookRepository.findByIdAndIsActive(999L, true))
                .willReturn(Optional.empty());

        // When & Then: 예외 발생 확인
        assertThatThrownBy(() ->
                transactionService.createTransaction(testUser.getId(), request))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    @DisplayName("다른사용자_장부접근_권한없음_예외발생")
    void createTransaction_UnauthorizedAccess_ThrowsException() {
        //  Given: 다른 사용자의 장부
        User anotherUser = User.builder()
                .id(999L)
                .email("another@test.com")
                .name("다른유저")
                .build();

        Book anotherBook = Book.builder()
                .id(1L)
                .user(anotherUser)
                .bookType(BookType.PERSONAL)
                .name("다른장부")
                .build();

        TransactionCreateRequest request = TransactionCreateRequest.builder()
                .bookId(1L)
                .date(LocalDate.now())
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100000"))
                .categoryId(1L)
                .paymentMethodId(2L)
                .build();

        // Mock: 다른 사용자의 장부 반환
        given(bookRepository.findByIdAndIsActive(1L, true))
                .willReturn(Optional.of(anotherBook));

        // When & Then: UnauthorizedBookAccessException 발생
        assertThatThrownBy(() ->
                transactionService.createTransaction(testUser.getId(), request))
                .isInstanceOf(UnauthorizedBookAccessException.class);
    }

    @Test
    @DisplayName("존재하지않는_계정과목_예외발생")
    void createTransaction_AccountNotFound_ThrowsException() {
        // Given : 존재하지 않는 계정과목 ID
        TransactionCreateRequest request = TransactionCreateRequest.builder()
                .bookId(1L)
                .date(LocalDate.now())
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100000"))
                .categoryId(999L)
                .paymentMethodId(2L)
                .build();

        // Mock: 장부는 정상, 계정과목은 없음
        given(bookRepository.findByIdAndIsActive(1L, true))
                .willReturn(Optional.of(testBook));

        given(accountRepository.findById(999L))
                .willReturn(Optional.empty());

        // When & Then: AccountNotFoundException 발생
        assertThatThrownBy(() ->
                transactionService.createTransaction(testUser.getId(), request))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("수입거래_비용계정사용_예외발생")
    void createTransaction_InvalidAccountType_ThrowsException() {
        // Given: 수입인데 비용 계정 사용
        TransactionCreateRequest request = TransactionCreateRequest.builder()
                .bookId(1L)
                .date(LocalDate.now())
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100000"))
                .categoryId(3L)
                .paymentMethodId(2L)
                .build();

        given(bookRepository.findByIdAndIsActive(1L, true))
                .willReturn(Optional.of(testBook));

        given(accountRepository.findById(3L))
                .willReturn(Optional.of(expenseAccount));

        given(accountRepository.findById(2L))
                .willReturn(Optional.of(paymentAccount));

        // When & Then
        assertThatThrownBy(() ->
                transactionService.createTransaction(testUser.getId(), request))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("수익 계정과목을 사용해야 합니다");
    }

    @Test
    @DisplayName("장부타입_계정타입_불일치_예외발생")
    void createTransaction_BookTypeMismatch_ThrowsException() {
        // Given: 개인 장부인데 사업자 계정 사용
        Account businessAccount = Account.builder()
                .id(99L)
                .code("7100")
                .name("사업자수익")
                .accountType(AccountType.REVENUE)
                .bookType(BookType.BUSINESS)  // 사업자 계정
                .build();

        TransactionCreateRequest request = TransactionCreateRequest.builder()
                .bookId(1L)  // PERSONAL 장부
                .date(LocalDate.now())
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100000"))
                .categoryId(99L)  // BUSINESS 계정
                .paymentMethodId(2L)
                .build();

        given(bookRepository.findByIdAndIsActive(1L, true))
                .willReturn(Optional.of(testBook));  // PERSONAL

        given(accountRepository.findById(99L))
                .willReturn(Optional.of(businessAccount));  // BUSINESS

        given(accountRepository.findById(2L))
                .willReturn(Optional.of(paymentAccount));

        // When & Then
        assertThatThrownBy(() ->
                transactionService.createTransaction(testUser.getId(), request))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("장부 타입")
                .hasMessageContaining("일치하지 않습니다");
    }
}