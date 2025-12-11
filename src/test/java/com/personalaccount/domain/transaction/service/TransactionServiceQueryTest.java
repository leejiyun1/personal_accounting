package com.personalaccount.domain.transaction.service;

import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.entity.BookType;
import com.personalaccount.domain.book.repository.BookRepository;
import com.personalaccount.domain.transaction.dto.request.TransactionSearchCondition;
import com.personalaccount.domain.transaction.dto.response.TransactionResponse;
import com.personalaccount.domain.transaction.entity.Transaction;
import com.personalaccount.domain.transaction.entity.TransactionType;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService 조회 테스트")
public class TransactionServiceQueryTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Captor
    private ArgumentCaptor<TransactionSearchCondition> conditionCaptor;

    private User testUser;
    private Book testBook;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("테스트유저")
                .build();

        testBook = Book.builder()
                .id(1L)
                .user(testUser)
                .bookType(BookType.PERSONAL)
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
        // Given
        Long userId = 1L;
        Long bookId = 1L;
        TransactionType type = TransactionType.INCOME;
        LocalDate startDate = LocalDate.of(2024,1,1);
        LocalDate endDate = LocalDate.of(2024,12,31);

        given(bookRepository.findByIdAndIsActive(1L, true))
                .willReturn(Optional.of(testBook));

        given(transactionRepository.searchTransactions(any(TransactionSearchCondition.class)))
                .willReturn(List.of(testTransaction));

        // When
        List<TransactionResponse> result = transactionService.getTransactions(
                userId,bookId,type,startDate,endDate);

        //Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getType()).isEqualTo(TransactionType.INCOME);

        // Repository 호출 확인
        verify(bookRepository).findByIdAndIsActive(1L, true);
        verify(transactionRepository).searchTransactions(conditionCaptor.capture());

        // 검색 조건 확인
        TransactionSearchCondition capturedCondition = conditionCaptor.getValue();
        assertThat(capturedCondition.getBookId()).isEqualTo(bookId);
        assertThat(capturedCondition.getType()).isEqualTo(type);
        assertThat(capturedCondition.getStartDate()).isEqualTo(startDate);
        assertThat(capturedCondition.getEndDate()).isEqualTo(endDate);
    }

    @Test
    @DisplayName("거래단건조회_성공")
    void getTransaction_Success() {
        // Given
        Long userId = 1L;
        Long transactionId = 1L;

        given(transactionRepository.findByIdWithBookAndUser(1L))
                .willReturn(Optional.of(testTransaction));

        // When
        TransactionResponse result = transactionService.getTransaction(userId, transactionId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo(TransactionType.INCOME);
        assertThat(result.getAmount()).isEqualByComparingTo("100000");

        verify(transactionRepository).findByIdWithBookAndUser(1L);
    }
}
