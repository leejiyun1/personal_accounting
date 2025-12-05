package com.personalaccount.domain.book.service;

import com.personalaccount.common.exception.custom.BookNotFoundException;
import com.personalaccount.common.exception.custom.DuplicateBookTypeException;
import com.personalaccount.common.exception.custom.UnauthorizedBookAccessException;
import com.personalaccount.common.exception.custom.UserNotFoundException;
import com.personalaccount.domain.account.repository.AccountRepository;
import com.personalaccount.domain.book.dto.request.BookCreateRequest;
import com.personalaccount.domain.book.dto.request.BookUpdateRequest;
import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.entity.BookType;
import com.personalaccount.domain.book.repository.BookRepository;
import com.personalaccount.domain.book.service.impl.BookServiceImpl;
import com.personalaccount.domain.user.entity.User;
import com.personalaccount.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService 테스트")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private User testUser;
    private Book testBook;
    private BookCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("테스트")
                .password("password")
                .build();

        testBook = Book.builder()
                .id(1L)
                .name("내 장부")
                .bookType(BookType.PERSONAL)
                .user(testUser)
                .isActive(true)
                .build();

        createRequest = BookCreateRequest.builder()
                .name("내 장부")
                .bookType(BookType.PERSONAL)
                .build();
    }

    @Test
    @DisplayName("장부생성_성공")
    void createBook_Success() {
        // Given
        Long userId = 1L;

        given(userRepository.existsById(userId)).willReturn(true);
        given(bookRepository.findByUserIdAndBookTypeAndIsActive(userId, BookType.PERSONAL, true))
                .willReturn(Optional.empty());
        given(userRepository.getReferenceById(userId)).willReturn(testUser);
        given(bookRepository.save(any(Book.class))).willReturn(testBook);

        // When
        Book result = bookService.createBook(userId, createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("내 장부");
        assertThat(result.getBookType()).isEqualTo(BookType.PERSONAL);
        assertThat(result.getUser().getId()).isEqualTo(userId);

        verify(userRepository).existsById(userId);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("장부생성_기본계정과목_자동생성_확인")
    void createBook_DefaultAccounts_Created() {
        // Given
        Long userId = 1L;

        given(userRepository.existsById(userId)).willReturn(true);
        given(bookRepository.findByUserIdAndBookTypeAndIsActive(userId, BookType.PERSONAL, true))
                .willReturn(Optional.empty());
        given(userRepository.getReferenceById(userId)).willReturn(testUser);
        given(bookRepository.save(any(Book.class))).willReturn(testBook);

        // When
        bookService.createBook(userId, createRequest);

        // Then
        verify(accountRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("장부생성_사용자없음_예외발생")
    void createBook_UserNotFound_ThrowsException() {
        // Given
        Long userId = 999L;

        given(userRepository.existsById(userId)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> bookService.createBook(userId, createRequest))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).existsById(userId);
    }

    @Test
    @DisplayName("장부생성_중복BookType_예외발생")
    void createBook_DuplicateBookType_ThrowsException() {
        // Given
        Long userId = 1L;

        given(userRepository.existsById(userId)).willReturn(true);
        given(bookRepository.findByUserIdAndBookTypeAndIsActive(userId, BookType.PERSONAL, true))
                .willReturn(Optional.of(testBook));

        // When & Then
        assertThatThrownBy(() -> bookService.createBook(userId, createRequest))
                .isInstanceOf(DuplicateBookTypeException.class);

        verify(userRepository).existsById(userId);
        verify(bookRepository).findByUserIdAndBookTypeAndIsActive(userId, BookType.PERSONAL, true);
    }

    @Test
    @DisplayName("장부목록조회_성공")
    void getBooksByUserId_Success() {
        // Given
        Long userId = 1L;
        List<Book> books = List.of(testBook);

        given(bookRepository.findByUserIdAndIsActive(userId, true)).willReturn(books);

        // When
        List<Book> result = bookService.getBooksByUserId(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("내 장부");

        verify(bookRepository).findByUserIdAndIsActive(userId, true);
    }

    @Test
    @DisplayName("장부단건조회_성공")
    void getBook_Success() {
        // Given
        Long bookId = 1L;
        Long userId = 1L;

        given(bookRepository.findByIdAndIsActive(bookId, true)).willReturn(Optional.of(testBook));

        // When
        Book result = bookService.getBook(bookId, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(bookId);
        assertThat(result.getName()).isEqualTo("내 장부");

        verify(bookRepository).findByIdAndIsActive(bookId, true);
    }

    @Test
    @DisplayName("장부단건조회_장부없음_예외발생")
    void getBook_NotFound_ThrowsException() {
        // Given
        Long bookId = 999L;
        Long userId = 1L;

        given(bookRepository.findByIdAndIsActive(bookId, true)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookService.getBook(bookId, userId))
                .isInstanceOf(BookNotFoundException.class);

        verify(bookRepository).findByIdAndIsActive(bookId, true);
    }

    @Test
    @DisplayName("장부단건조회_권한없음_예외발생")
    void getBook_Unauthorized_ThrowsException() {
        // Given
        Long bookId = 1L;
        Long userId = 999L;

        given(bookRepository.findByIdAndIsActive(bookId, true)).willReturn(Optional.of(testBook));

        // When & Then
        assertThatThrownBy(() -> bookService.getBook(bookId, userId))
                .isInstanceOf(UnauthorizedBookAccessException.class);

        verify(bookRepository).findByIdAndIsActive(bookId, true);
    }

    @Test
    @DisplayName("장부수정_성공")
    void updateBook_Success() {
        // Given
        Long bookId = 1L;
        Long userId = 1L;
        BookUpdateRequest updateRequest = BookUpdateRequest.builder()
                .name("수정된 장부")
                .build();

        given(bookRepository.findByIdAndIsActive(bookId, true)).willReturn(Optional.of(testBook));

        // When
        Book result = bookService.updateBook(bookId, userId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("수정된 장부");

        verify(bookRepository).findByIdAndIsActive(bookId, true);
    }

    @Test
    @DisplayName("장부삭제_SoftDelete_성공")
    void deleteBook_Success() {
        // Given
        Long bookId = 1L;
        Long userId = 1L;

        given(bookRepository.findByIdAndIsActive(bookId, true)).willReturn(Optional.of(testBook));

        // When
        bookService.deleteBook(bookId, userId);

        // Then
        assertThat(testBook.getIsActive()).isFalse();

        verify(bookRepository).findByIdAndIsActive(bookId, true);
    }
}