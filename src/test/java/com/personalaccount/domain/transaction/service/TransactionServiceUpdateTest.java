package com.personalaccount.domain.transaction.service;

import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.entity.BookType;
import com.personalaccount.domain.transaction.dto.mapper.TransactionMapper;
import com.personalaccount.domain.transaction.dto.request.TransactionUpdateRequest;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService 수정/삭제 테스트")
class TransactionServiceUpdateTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User testUser;
    private Book testBook;
    private Transaction testTransaction;
    private TransactionResponse testResponse;

    @BeforeEach
    void setup() {
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
                .memo("원래메모")
                .build();

        testResponse = TransactionResponse.builder()
                .id(1L)
                .bookId(1L)
                .date(LocalDate.now())
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100000"))
                .memo("수정된메모")
                .build();
    }

    @Test
    @DisplayName("거래수정_메모변경_성공")
    void updateTransaction_Success() {
        // Given
        Long userId = 1L;
        Long transactionId = 1L;
        TransactionUpdateRequest request = TransactionUpdateRequest.builder()
                .memo("수정된메모")
                .build();

        given(transactionRepository.findByIdWithBookAndUser(1L))
                .willReturn(Optional.of(testTransaction));

        given(transactionMapper.toResponse(testTransaction))
                .willReturn(testResponse);

        // When
        TransactionResponse result = transactionService.updateTransaction(userId, transactionId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMemo()).isEqualTo("수정된메모");

        verify(transactionRepository).findByIdWithBookAndUser(1L);
        verify(transactionMapper).toResponse(testTransaction);
    }

    @Test
    @DisplayName("거래삭제_SoftDelete_성공")
    void deleteTransaction_Success() {
        // Given
        Long userId = 1L;
        Long transactionId = 1L;

        given(transactionRepository.findByIdWithBookAndUser(1L))
                .willReturn(Optional.of(testTransaction));

        // When
        transactionService.deleteTransaction(userId, transactionId);

        // Then
        assertThat(testTransaction.getIsActive()).isFalse();

        verify(transactionRepository).findByIdWithBookAndUser(1L);
    }
}