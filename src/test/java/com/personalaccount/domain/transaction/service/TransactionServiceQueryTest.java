package com.personalaccount.domain.transaction.service;

import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.repository.BookRepository;
import com.personalaccount.domain.transaction.dto.mapper.TransactionMapper;
import com.personalaccount.domain.transaction.dto.request.TransactionSearchCondition;
import com.personalaccount.domain.transaction.dto.response.TransactionDetailResponse;
import com.personalaccount.domain.transaction.dto.response.TransactionResponse;
import com.personalaccount.domain.transaction.entity.DetailType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService 조회 테스트")
class TransactionServiceQueryTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private TransactionDetailRepository transactionDetailRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Book testBook;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("테스트유저")
                .build();

        testBook = Book.builder()
                .id(1L)
                .user(testUser)
                .name("개인장부")
                .build();

        testTransaction = Transaction.builder()
                .id(1L)
                .book(testBook)
                .date(LocalDate.now())
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100000"))
                .memo("테스트거래")
                .build();
    }

    @Test
    @DisplayName("거래목록조회_성공")
    void getTransactions_Success() {
        Long userId = 1L;
        Long bookId = 1L;
        TransactionType type = TransactionType.INCOME;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        TransactionResponse expectedResponse = TransactionResponse.builder()
                .id(1L)
                .bookId(1L)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100000"))
                .date(LocalDate.now())
                .memo("테스트거래")
                .build();

        given(bookRepository.findByIdAndIsActive(bookId, true))
                .willReturn(Optional.of(testBook));
        given(transactionRepository.searchTransactions(any(TransactionSearchCondition.class)))
                .willReturn(List.of(testTransaction));
        given(transactionMapper.toResponse(testTransaction))
                .willReturn(expectedResponse);

        List<TransactionResponse> result = transactionService.getTransactions(
                userId, bookId, type, startDate, endDate);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getBookId()).isEqualTo(bookId);
        assertThat(result.getFirst().getType()).isEqualTo(type);

        verify(bookRepository).findByIdAndIsActive(bookId, true);
        verify(transactionRepository).searchTransactions(any(TransactionSearchCondition.class));
        verify(transactionMapper).toResponse(testTransaction);
    }

    @Test
    @DisplayName("거래단건조회_성공")
    void getTransaction_Success() {
        Long userId = 1L;
        Long transactionId = 1L;

        TransactionResponse expectedResponse = TransactionResponse.builder()
                .id(1L)
                .bookId(1L)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100000"))
                .date(LocalDate.now())
                .memo("테스트거래")
                .build();

        given(transactionRepository.findByIdWithBookAndUser(transactionId))
                .willReturn(Optional.of(testTransaction));
        given(transactionMapper.toResponse(testTransaction))
                .willReturn(expectedResponse);

        TransactionResponse result = transactionService.getTransaction(userId, transactionId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(transactionId);
        assertThat(result.getBookId()).isEqualTo(1L);

        verify(transactionRepository).findByIdWithBookAndUser(transactionId);
        verify(transactionMapper).toResponse(testTransaction);
    }

    @Test
    @DisplayName("거래_복식부기_상세조회_성공")
    void getTransactionWithDetails_Success() {
        Long userId = 1L;
        Long transactionId = 1L;

        JournalEntry journalEntry = JournalEntry.builder()
                .id(1L)
                .transaction(testTransaction)
                .date(LocalDate.now())
                .description("수입 - 급여 100000원")
                .build();

        TransactionDetail debitDetail = TransactionDetail.builder()
                .id(1L)
                .journalEntry(journalEntry)
                .detailType(DetailType.DEBIT)
                .debitAmount(new BigDecimal("100000"))
                .creditAmount(BigDecimal.ZERO)
                .build();

        TransactionDetail creditDetail = TransactionDetail.builder()
                .id(2L)
                .journalEntry(journalEntry)
                .detailType(DetailType.CREDIT)
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(new BigDecimal("100000"))
                .build();

        TransactionDetailResponse expectedResponse = TransactionDetailResponse.builder()
                .id(1L)
                .bookId(1L)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100000"))
                .date(LocalDate.now())
                .memo("테스트거래")
                .build();

        given(transactionRepository.findByIdWithBookAndUser(transactionId))
                .willReturn(Optional.of(testTransaction));
        given(journalEntryRepository.findByTransactionId(transactionId))
                .willReturn(List.of(journalEntry));
        given(transactionDetailRepository.findWithAccountByJournalEntryIdIn(List.of(1L)))
                .willReturn(List.of(debitDetail, creditDetail));
        given(transactionMapper.toDetailResponse(
                testTransaction,
                List.of(journalEntry),
                List.of(List.of(debitDetail, creditDetail))))
                .willReturn(expectedResponse);

        TransactionDetailResponse result = transactionService.getTransactionWithDetails(userId, transactionId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(transactionId);

        verify(transactionRepository).findByIdWithBookAndUser(transactionId);
        verify(journalEntryRepository).findByTransactionId(transactionId);
        verify(transactionDetailRepository).findWithAccountByJournalEntryIdIn(List.of(1L));
    }
}