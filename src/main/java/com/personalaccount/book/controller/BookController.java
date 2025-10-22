package com.personalaccount.book.controller;

import com.personalaccount.book.dto.mapper.BookMapper;
import com.personalaccount.book.dto.request.BookCreateRequest;
import com.personalaccount.book.dto.request.BookUpdateRequest;
import com.personalaccount.book.dto.response.BookResponse;
import com.personalaccount.book.entity.Book;
import com.personalaccount.book.service.BookService;
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

        // 1. Service 호출
        Book book = bookService.createBook(userId, request);

        // 2. Entity → DTO 변환
        BookResponse response = BookMapper.toResponse(book);

        // 3. CommonResponse로 감싸서 반환
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseFactory.success(response, "장부 생성 완료"));
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<BookResponse>>> getBooks(
            @AuthenticationPrincipal Long userId
    ) {
        log.info("장부 목록 조회 API 호출: userId={}", userId);

        // 1. Service 호출
        List<Book> books = bookService.getBooksByUserId(userId);

        // 2. Entity List → DTO List 변환
        List<BookResponse> response = books.stream()
                .map(BookMapper::toResponse)
                .toList();

        // 3. CommonResponse로 감싸서 반환
        return ResponseEntity.ok(
                ResponseFactory.success(response)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<BookResponse>> getBook(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("장부 조회 API 호출: bookId={}, userId={}", id, userId);

        // 1. Service 호출
        Book book = bookService.getBook(id, userId);

        // 2. Entity → DTO 변환
        BookResponse response = BookMapper.toResponse(book);

        // 3. CommonResponse로 감싸서 반환
        return ResponseEntity.ok(
                ResponseFactory.success(response)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<BookResponse>> updateBook(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody BookUpdateRequest request
    ) {
        log.info("장부 수정 API 호출: bookId={}, userId={}", id, userId);

        // 1. Service 호출
        Book book = bookService.updateBook(id, userId, request);

        // 2. Entity → DTO 변환
        BookResponse response = BookMapper.toResponse(book);

        // 3. CommonResponse로 감싸서 반환
        return ResponseEntity.ok(
                ResponseFactory.success(response, "장부 수정 완료")
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteBook(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("장부 삭제 API 호출: bookId={}, userId={}", id, userId);

        // 1. Service 호출
        bookService.deleteBook(id, userId);

        // 2. 성공 응답
        return ResponseEntity.ok(
                ResponseFactory.successWithMessage("장부 삭제 완료")
        );
    }
}