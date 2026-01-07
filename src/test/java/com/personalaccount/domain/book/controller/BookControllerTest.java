package com.personalaccount.domain.book.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalaccount.domain.book.dto.mapper.BookMapper;
import com.personalaccount.domain.book.dto.request.BookCreateRequest;
import com.personalaccount.domain.book.dto.request.BookUpdateRequest;
import com.personalaccount.domain.book.dto.response.BookResponse;
import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.entity.BookType;
import com.personalaccount.domain.book.service.BookService;
import com.personalaccount.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookController 테스트")
class BookControllerTest {

    @Mock
    private BookService bookService;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookController bookController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return 1L;
                    }
                })
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("장부_생성_성공")
    void createBook_Success() throws Exception {
        BookCreateRequest request = BookCreateRequest.builder()
                .bookType(BookType.PERSONAL)
                .name("새 장부")
                .build();

        User testUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("테스터")
                .build();

        Book testBook = Book.builder()
                .id(1L)
                .user(testUser)
                .bookType(BookType.PERSONAL)
                .name("개인장부")
                .build();

        BookResponse testResponse = BookResponse.builder()
                .id(1L)
                .userId(1L)
                .bookType(BookType.PERSONAL)
                .name("개인장부")
                .build();

        given(bookService.createBook(eq(1L), any(BookCreateRequest.class))).willReturn(testBook);
        given(bookMapper.toResponse(testBook)).willReturn(testResponse);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(bookService).createBook(eq(1L), any(BookCreateRequest.class));
    }

    @Test
    @DisplayName("장부_목록_조회_성공")
    void getBooks_Success() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("테스터")
                .build();

        Book testBook = Book.builder()
                .id(1L)
                .user(testUser)
                .bookType(BookType.PERSONAL)
                .name("개인장부")
                .build();

        BookResponse testResponse = BookResponse.builder()
                .id(1L)
                .userId(1L)
                .bookType(BookType.PERSONAL)
                .name("개인장부")
                .build();

        given(bookService.getBooksByUserId(1L)).willReturn(List.of(testBook));
        given(bookMapper.toResponse(testBook)).willReturn(testResponse);

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(bookService).getBooksByUserId(1L);
    }

    @Test
    @DisplayName("장부_단건_조회_성공")
    void getBook_Success() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("테스터")
                .build();

        Book testBook = Book.builder()
                .id(1L)
                .user(testUser)
                .bookType(BookType.PERSONAL)
                .name("개인장부")
                .build();

        BookResponse testResponse = BookResponse.builder()
                .id(1L)
                .userId(1L)
                .bookType(BookType.PERSONAL)
                .name("개인장부")
                .build();

        given(bookService.getBook(1L, 1L)).willReturn(testBook);
        given(bookMapper.toResponse(testBook)).willReturn(testResponse);

        mockMvc.perform(get("/api/v1/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(bookService).getBook(1L, 1L);
    }

    @Test
    @DisplayName("장부_수정_성공")
    void updateBook_Success() throws Exception {
        BookUpdateRequest request = BookUpdateRequest.builder()
                .name("수정된 장부")
                .build();

        User testUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("테스터")
                .build();

        Book testBook = Book.builder()
                .id(1L)
                .user(testUser)
                .bookType(BookType.PERSONAL)
                .name("개인장부")
                .build();

        BookResponse testResponse = BookResponse.builder()
                .id(1L)
                .userId(1L)
                .bookType(BookType.PERSONAL)
                .name("개인장부")
                .build();

        given(bookService.updateBook(eq(1L), eq(1L), any(BookUpdateRequest.class))).willReturn(testBook);
        given(bookMapper.toResponse(testBook)).willReturn(testResponse);

        mockMvc.perform(put("/api/v1/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(bookService).updateBook(eq(1L), eq(1L), any(BookUpdateRequest.class));
    }

    @Test
    @DisplayName("장부_삭제_성공")
    void deleteBook_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(bookService).deleteBook(1L, 1L);
    }
}