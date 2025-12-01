package com.personalaccount.domain.transaction.service;

import com.personalaccount.common.exception.custom.AccountNotFoundException;
import com.personalaccount.common.exception.custom.BookNotFoundException;
import com.personalaccount.domain.account.entity.Account;
import com.personalaccount.domain.account.entity.AccountType;
import com.personalaccount.domain.account.repository.AccountRepository;
import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.entity.BookType;
import com.personalaccount.domain.book.repository.BookRepository;
import com.personalaccount.domain.transaction.dto.request.TransactionCreateRequest;
import com.personalaccount.domain.transaction.entity.*;
import com.personalaccount.domain.transaction.repository.JournalEntryRepository;
import com.personalaccount.domain.transaction.repository.TransactionDetailRepository;
import com.personalaccount.domain.transaction.repository.TransactionRepository;
import com.personalaccount.domain.transaction.service.impl.TransactionServiceImpl;
import com.personalaccount.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService 테스트")
class TransactionServiceTest {

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

    @Captor
    private ArgumentCaptor<List<TransactionDetail>> detailsCaptor;

    private User testUser;
    private Book testBook;
    private Account revenueAccount;
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

        // 결제수단 계정 (보통예금)
        paymentAccount = Account.builder()
                .id(2L)
                .code("1010")
                .name("보통예금")
                .accountType(AccountType.PAYMENT_METHOD)
                .bookType(BookType.PERSONAL)
                .build();
    }

    @Test
    @DisplayName("수입거래_복식부기_정상생성_전체검증")
    void createIncomeTransaction_Success() {
        // Given: 수입 거래 요청 데이터
        TransactionCreateRequest request = TransactionCreateRequest.builder()
                .bookId(1L)
                .date(LocalDate.now())
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("500000"))
                .categoryId(1L)
                .paymentMethodId(2L)
                .memo("월급")
                .build();

        // Mock 동작 정의
        given(bookRepository.findByIdAndIsActive(1L, true))
                .willReturn(Optional.of(testBook));

        given(accountRepository.findById(1L))
                .willReturn(Optional.of(revenueAccount));

        given(accountRepository.findById(2L))
                .willReturn(Optional.of(paymentAccount));

        given(transactionRepository.save(any(Transaction.class)))
                .willAnswer(invocation -> {
                    Transaction tx = invocation.getArgument(0);
                    return Transaction.builder()
                            .id(1L)
                            .book(tx.getBook())
                            .date(tx.getDate())
                            .type(tx.getType())
                            .amount(tx.getAmount())
                            .memo(tx.getMemo())
                            .build();
                });

        given(journalEntryRepository.save(any(JournalEntry.class)))
                .willAnswer(invocation -> {
                    JournalEntry je = invocation.getArgument(0);
                    return JournalEntry.builder()
                            .id(1L)
                            .transaction(je.getTransaction())
                            .date(je.getDate())
                            .description(je.getDescription())
                            .build();
                });

        // When: 거래 생성 실행
        Transaction result = transactionService.createTransaction(testUser.getId(), request);

        // Then: 기본 검증
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo(TransactionType.INCOME);
        assertThat(result.getAmount()).isEqualByComparingTo("500000");
        assertThat(result.getMemo()).isEqualTo("월급");

        // Repository 호출 횟수 검증
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(journalEntryRepository, times(1)).save(any(JournalEntry.class));
        verify(transactionDetailRepository, times(1)).saveAll(anyList());

        // ArgumentCaptor로 저장된 TransactionDetail 캡처
        verify(transactionDetailRepository).saveAll(detailsCaptor.capture());
        List<TransactionDetail> savedDetails = detailsCaptor.getValue();

        // 2개 생성 확인
        assertThat(savedDetails).hasSize(2);

        // 차변/대변 분리
        TransactionDetail debitDetail = savedDetails.stream()
                .filter(d -> d.getDetailType() == DetailType.DEBIT)
                .findFirst()
                .orElseThrow(() -> new AssertionError("차변 내역이 없습니다"));

        TransactionDetail creditDetail = savedDetails.stream()
                .filter(d -> d.getDetailType() == DetailType.CREDIT)
                .findFirst()
                .orElseThrow(() -> new AssertionError("대변 내역이 없습니다"));

        // 차변 검증 (보통예금 - 결제수단)
        assertThat(debitDetail.getAccount().getId()).isEqualTo(2L);
        assertThat(debitDetail.getAccount().getName()).isEqualTo("보통예금");
        assertThat(debitDetail.getDebitAmount()).isEqualByComparingTo("500000");
        assertThat(debitDetail.getCreditAmount()).isEqualByComparingTo("0");

        // 대변 검증 (급여 - 수익)
        assertThat(creditDetail.getAccount().getId()).isEqualTo(1L);
        assertThat(creditDetail.getAccount().getName()).isEqualTo("급여");
        assertThat(creditDetail.getDebitAmount()).isEqualByComparingTo("0");
        assertThat(creditDetail.getCreditAmount()).isEqualByComparingTo("500000");

        // 대차평형 검증 (차변 합계 = 대변 합계)
        BigDecimal totalDebit = savedDetails.stream()
                .map(TransactionDetail::getDebitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredit = savedDetails.stream()
                .map(TransactionDetail::getCreditAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(totalDebit).isEqualByComparingTo(totalCredit);
        assertThat(totalDebit).isEqualByComparingTo("500000");
        assertThat(totalCredit).isEqualByComparingTo("500000");
    }
}