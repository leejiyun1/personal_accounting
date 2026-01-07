package com.personalaccount.application.ai.service;

import com.personalaccount.application.ai.chat.service.AiChatService;
import com.personalaccount.application.ai.chat.dto.request.AiChatRequest;
import com.personalaccount.application.ai.chat.dto.response.AiChatResponse;
import com.personalaccount.domain.account.entity.Account;
import com.personalaccount.domain.account.entity.AccountType;
import com.personalaccount.domain.account.repository.AccountRepository;
import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.entity.BookType;
import com.personalaccount.domain.book.repository.BookRepository;
import com.personalaccount.domain.transaction.entity.TransactionType;
import com.personalaccount.domain.user.entity.User;
import com.personalaccount.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Gemini API 할당량 제한으로 임시 비활성화")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AI 대화 통합 테스트")
class AiChatServiceIntegrationTest {

    @Autowired
    private AiChatService aiChatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AccountRepository accountRepository;

    private User testUser;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@test.com")
                .password("password")
                .name("테스터")
                .isActive(true)
                .build();
        testUser = userRepository.save(testUser);

        testBook = Book.builder()
                .user(testUser)
                .name("테스트 장부")
                .bookType(BookType.PERSONAL)
                .isActive(true)
                .build();
        testBook = bookRepository.save(testBook);

        Account incomeAccount = Account.builder()
                .code("REV001")
                .name("부수입")
                .accountType(AccountType.REVENUE)
                .bookType(BookType.PERSONAL)
                .isActive(true)
                .build();
        accountRepository.save(incomeAccount);

        Account cashAccount = Account.builder()
                .code("AST001")
                .name("현금")
                .accountType(AccountType.PAYMENT_METHOD)
                .bookType(BookType.PERSONAL)
                .isActive(true)
                .build();
        accountRepository.save(cashAccount);
    }

    @Test
    @DisplayName("자연어_입력_수입기록_성공")
    void testNaturalLanguageIncome() {
        AiChatRequest request = AiChatRequest.builder()
                .bookId(testBook.getId())
                .message("오늘 부수입으로 30만원 벌었어")
                .build();

        AiChatResponse response = aiChatService.chat(testUser.getId(), request);

        assertThat(response).isNotNull();

        if (response.getNeedsMoreInfo()) {
            assertThat(response.getMessage()).isNotBlank();
            assertThat(response.getConversationId()).isNotNull();

            AiChatRequest followUpRequest = AiChatRequest.builder()
                    .bookId(testBook.getId())
                    .conversationId(response.getConversationId())
                    .message("현금으로 받았어")
                    .build();

            response = aiChatService.chat(testUser.getId(), followUpRequest);
        }

        assertThat(response.getNeedsMoreInfo()).isFalse();
        assertThat(response.getTransaction()).isNotNull();
        assertThat(response.getTransaction().getType()).isEqualTo(TransactionType.INCOME);
        assertThat(response.getTransaction().getAmount()).isEqualTo(new BigDecimal("300000"));
    }

    @Test
    @DisplayName("자연어_입력_불완전한_정보로_대화_시작")
    void testIncompleteInformation() {
        AiChatRequest request = AiChatRequest.builder()
                .bookId(testBook.getId())
                .message("돈 벌었어")
                .build();

        AiChatResponse response = aiChatService.chat(testUser.getId(), request);

        assertThat(response).isNotNull();
        assertThat(response.getNeedsMoreInfo()).isTrue();
        assertThat(response.getConversationId()).isNotNull();
        assertThat(response.getMessage()).isNotBlank();
    }

    @Test
    @DisplayName("자연어_입력_완전한_정보_한번에_제공")
    void testCompleteInformationAtOnce() {
        AiChatRequest request = AiChatRequest.builder()
                .bookId(testBook.getId())
                .message("오늘 부수입으로 30만원을 현금으로 받았어")
                .build();

        AiChatResponse response = aiChatService.chat(testUser.getId(), request);

        if (!response.getNeedsMoreInfo()) {
            assertThat(response.getTransaction()).isNotNull();
            assertThat(response.getTransaction().getAmount()).isEqualTo(new BigDecimal("300000"));
        }
    }
}