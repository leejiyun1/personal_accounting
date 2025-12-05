package com.personalaccount.domain.book.controller;

import com.personalaccount.domain.book.dto.mapper.BookMapper;
import com.personalaccount.domain.book.dto.request.BookCreateRequest;
import com.personalaccount.domain.book.dto.request.BookUpdateRequest;
import com.personalaccount.domain.book.dto.response.BookResponse;
import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.service.BookService;
import com.personalaccount.common.dto.CommonResponse;
import com.personalaccount.common.dto.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<CommonResponse<BookResponse>> createBook(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody BookCreateRequest request
    ) {
        log.info("장부 생성 API 호출: userId={}, bookType={}", userId, request.getBookType());

        Book book = bookService.createBook(userId, request);
        BookResponse response = BookMapper.toResponse(book);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseFactory.success(response, "장부 생성 완료"));
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<BookResponse>>> getBooks(
            @AuthenticationPrincipal Long userId
    ) {
        log.info("장부 목록 조회 API 호출: userId={}", userId);

        List<Book> books = bookService.getBooksByUserId(userId);
        List<BookResponse> response = books.stream()
                .map(BookMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<BookResponse>> getBook(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("장부 조회 API 호출: bookId={}, userId={}", id, userId);

        Book book = bookService.getBook(id, userId);
        BookResponse response = BookMapper.toResponse(book);

        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<BookResponse>> updateBook(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody BookUpdateRequest request
    ) {
        log.info("장부 수정 API 호출: bookId={}, userId={}", id, userId);

        Book book = bookService.updateBook(id, userId, request);
        BookResponse response = BookMapper.toResponse(book);

        return ResponseEntity.ok(ResponseFactory.success(response, "장부 수정 완료"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteBook(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("장부 삭제 API 호출: bookId={}, userId={}", id, userId);

        bookService.deleteBook(id, userId);

        return ResponseEntity.ok(ResponseFactory.successWithMessage("장부 삭제 완료"));
    }
}