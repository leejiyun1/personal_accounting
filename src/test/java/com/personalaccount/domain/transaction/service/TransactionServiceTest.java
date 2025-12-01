package com.personalaccount.domain.transaction.service;

import com.personalaccount.domain.account.entity.Account;
import com.personalaccount.domain.account.entity.AccountType;
import com.personalaccount.domain.account.repository.AccountRepository;
import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.entity.BookType;
import com.personalaccount.domain.book.repository.BookRepository;
import com.personalaccount.domain.transaction.dto.request.TransactionCreateRequest;
import com.personalaccount.domain.transaction.entity.JournalEntry;
import com.personalaccount.domain.transaction.entity.Transaction;
import com.personalaccount.domain.transaction.entity.TransactionDetail;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

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
    private User testUser;
    private Book testBook;
    private Account revenueAccount;
    private Account paymentAccount;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .password("password")
                .name("테스트유저")
                .build();

        testBook = Book.builder()
                .id(1L)
                .user(testUser)
                .name("테스트 장부")
                .bookType(BookType.PERSONAL)
                .isActive(true)
                .build();

        revenueAccount = Account.builder()
                .id(1L)
                .accountType(AccountType.REVENUE)
                .name("급여")
                .code("5100")
                .bookType(BookType.PERSONAL)
                .isActive(true)
                .build();

        paymentAccount = Account.builder()
                .id(2L)
                .accountType(AccountType.PAYMENT_METHOD)
                .name("보통예금")
                .code("1010")
                .bookType(BookType.PERSONAL)
                .isActive(true)
                .build();
    }
    @Test
    @DisplayName("수입거래_복식부기_정상생성")
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
        given(transactionDetailRepository.save(any(TransactionDetail.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        given(transactionDetailRepository.findByJournalEntryId(1L))
                .willReturn(List.of());

        // When: 거래 생성 실행
        Transaction result = transactionService.createTransaction(testUser.getId(), request);

        // Them: 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(TransactionType.INCOME);
        assertThat(result.getAmount()).isEqualByComparingTo("500000");
    }

}
