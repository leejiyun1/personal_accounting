package com.personalaccount.application.ai.service;

import com.personalaccount.application.ai.dto.request.AiChatRequest;
import com.personalaccount.application.ai.dto.response.AiChatResponse;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AI 대화 통합 테스트 - 실제 Gemini API 호출")
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
    private Account incomeAccount;
    private Account cashAccount;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .email("test@test.com")
                .password("password")
                .name("테스터")
                .isActive(true)
                .build();
        testUser = userRepository.save(testUser);

        // 테스트 장부 생성
        testBook = Book.builder()
                .user(testUser)
                .name("테스트 장부")
                .bookType(BookType.PERSONAL)
                .isActive(true)
                .build();
        testBook = bookRepository.save(testBook);

        // 수입 계정 생성
        incomeAccount = Account.builder()
                .code("REV001")
                .name("부수입")
                .accountType(AccountType.REVENUE)
                .bookType(BookType.PERSONAL)
                .isActive(true)
                .build();
        incomeAccount = accountRepository.save(incomeAccount);

        // 현금 계정 생성
        cashAccount = Account.builder()
                .code("AST001")
                .name("현금")
                .accountType(AccountType.PAYMENT_METHOD)
                .bookType(BookType.PERSONAL)
                .isActive(true)
                .build();
        cashAccount = accountRepository.save(cashAccount);
    }

    @Test
    @DisplayName("자연어 입력 - 수입 기록 성공")
    void testNaturalLanguageIncome() {
        // Given
        AiChatRequest request = AiChatRequest.builder()
                .bookId(testBook.getId())
                .message("오늘 부수입으로 30만원 벌었어")
                .build();

        // When
        AiChatResponse response = aiChatService.chat(testUser.getId(), request);

        // Then
        assertThat(response).isNotNull();

        // 추가 정보가 필요한 경우 (결제수단 등)
        if (response.getNeedsMoreInfo()) {
            assertThat(response.getMessage()).isNotBlank();
            assertThat(response.getConversationId()).isNotNull();
            System.out.println("AI 질문: " + response.getMessage());

            // 두 번째 응답 - 결제수단 제공
            AiChatRequest followUpRequest = AiChatRequest.builder()
                    .bookId(testBook.getId())
                    .conversationId(response.getConversationId())
                    .message("현금으로 받았어")
                    .build();

            response = aiChatService.chat(testUser.getId(), followUpRequest);
        }

        // 거래 생성 완료 검증
        assertThat(response.getNeedsMoreInfo()).isFalse();
        assertThat(response.getTransaction()).isNotNull();
        assertThat(response.getTransaction().getType()).isEqualTo(TransactionType.INCOME);
        assertThat(response.getTransaction().getAmount()).isEqualTo(new BigDecimal("300000"));

        System.out.println("생성된 거래: " + response.getTransaction());
    }

    @Test
    @DisplayName("자연어 입력 - 불완전한 정보로 대화 시작")
    void testIncompleteInformation() {
        // Given
        AiChatRequest request = AiChatRequest.builder()
                .bookId(testBook.getId())
                .message("돈 벌었어")
                .build();

        // When
        AiChatResponse response = aiChatService.chat(testUser.getId(), request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getNeedsMoreInfo()).isTrue();
        assertThat(response.getConversationId()).isNotNull();
        assertThat(response.getMessage()).isNotBlank();

        System.out.println("AI 질문: " + response.getMessage());
        // AI가 추가 정보를 요청해야 함 (금액, 카테고리 등)
    }

    @Test
    @DisplayName("자연어 입력 - 완전한 정보 한 번에 제공")
    void testCompleteInformationAtOnce() {
        // Given
        AiChatRequest request = AiChatRequest.builder()
                .bookId(testBook.getId())
                .message("오늘 부수입으로 30만원을 현금으로 받았어")
                .build();

        // When
        AiChatResponse response = aiChatService.chat(testUser.getId(), request);

        // Then
        // 충분한 정보가 있으면 바로 거래 생성
        if (!response.getNeedsMoreInfo()) {
            assertThat(response.getTransaction()).isNotNull();
            assertThat(response.getTransaction().getAmount()).isEqualTo(new BigDecimal("300000"));
            System.out.println("즉시 생성된 거래: " + response.getTransaction());
        } else {
            System.out.println("추가 정보 필요: " + response.getMessage());
        }
    }
}