package com.personalaccount.domain.transaction.repository;

import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.entity.BookType;
import com.personalaccount.domain.book.repository.BookRepository;
import com.personalaccount.domain.transaction.dto.request.TransactionSearchCondition;
import com.personalaccount.domain.transaction.entity.Transaction;
import com.personalaccount.domain.transaction.entity.TransactionType;
import com.personalaccount.domain.user.entity.User;
import com.personalaccount.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("TransactionRepository 통합 테스트")
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    private Book testBook;

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
                .email("test@test.com")
                .password("password")
                .name("테스터")
                .isActive(true)
                .build();
        testUser = userRepository.save(testUser);

        testBook = Book.builder()
                .user(testUser)
                .name("테스트장부")
                .bookType(BookType.PERSONAL)
                .isActive(true)
                .build();
        testBook = bookRepository.save(testBook);

        Transaction testTransaction = Transaction.builder()
                .book(testBook)
                .date(LocalDate.now())
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100000"))
                .memo("테스트거래")
                .isActive(true)
                .build();
        transactionRepository.save(testTransaction);
    }

    @Test
    @DisplayName("거래_검색_장부ID로_조회")
    void searchTransactions_ByBookId() {
        TransactionSearchCondition condition = TransactionSearchCondition.builder()
                .bookId(testBook.getId())
                .build();

        List<Transaction> result = transactionRepository.searchTransactions(condition);

        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().getBook().getId()).isEqualTo(testBook.getId());
    }

    @Test
    @DisplayName("거래_검색_타입으로_조회")
    void searchTransactions_ByType() {
        TransactionSearchCondition condition = TransactionSearchCondition.builder()
                .bookId(testBook.getId())
                .type(TransactionType.INCOME)
                .build();

        List<Transaction> result = transactionRepository.searchTransactions(condition);

        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().getType()).isEqualTo(TransactionType.INCOME);
    }

    @Test
    @DisplayName("거래_검색_키워드로_조회")
    void searchTransactions_ByKeyword() {
        TransactionSearchCondition condition = TransactionSearchCondition.builder()
                .bookId(testBook.getId())
                .keyword("테스트")
                .build();

        List<Transaction> result = transactionRepository.searchTransactions(condition);

        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().getMemo()).contains("테스트");
    }

    @Test
    @DisplayName("거래_검색_날짜범위로_조회")
    void searchTransactions_ByDateRange() {
        LocalDate today = LocalDate.now();
        TransactionSearchCondition condition = TransactionSearchCondition.builder()
                .bookId(testBook.getId())
                .startDate(today.minusDays(1))
                .endDate(today.plusDays(1))
                .build();

        List<Transaction> result = transactionRepository.searchTransactions(condition);

        assertThat(result).isNotEmpty();
    }
}