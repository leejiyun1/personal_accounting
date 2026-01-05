package com.personalaccount.domain.book.controller;

import com.personalaccount.domain.book.dto.mapper.BookMapper;
import com.personalaccount.domain.book.dto.request.BookCreateRequest;
import com.personalaccount.domain.book.dto.request.BookUpdateRequest;
import com.personalaccount.domain.book.dto.response.BookResponse;
import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.service.BookService;
import com.personalaccount.common.dto.CommonResponse;
import com.personalaccount.common.dto.ResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Book", description = "장부 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class BookController {

    private final BookService bookService;

    @Operation(
            summary = "장부 생성",
            description = "새로운 장부를 생성합니다. 같은 타입(개인/사업)의 장부는 1개만 생성 가능하며, " +
                    "생성 시 기본 계정과목이 자동으로 생성됩니다.\n\n" +
                    "**개인 장부**: 급여, 식비, 교통비 등 22개 계정과목\n" +
                    "**사업 장부**: 매출, 외주비, 임차료 등 23개 계정과목"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "장부 생성 성공",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미 같은 타입의 장부가 존재합니다"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패"
            )
    })
    @PostMapping
    public ResponseEntity<CommonResponse<BookResponse>> createBook(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody BookCreateRequest request
    ) {
        log.info("POST /api/v1/books - userId={}", userId);
        Book book = bookService.createBook(userId, request);
        BookResponse response = BookMapper.toResponse(book);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseFactory.success(response, "장부 생성 완료"));
    }

    @Operation(
            summary = "장부 목록 조회",
            description = "사용자의 활성화된 장부 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패"
            )
    })
    @GetMapping
    public ResponseEntity<CommonResponse<List<BookResponse>>> getBooks(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        log.info("GET /api/v1/books - userId={}", userId);
        List<Book> books = bookService.getBooksByUserId(userId);
        List<BookResponse> response = books.stream()
                .map(BookMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @Operation(
            summary = "장부 상세 조회",
            description = "장부 ID로 장부 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 장부에 접근 권한이 없습니다"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "장부를 찾을 수 없습니다"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<BookResponse>> getBook(
            @Parameter(description = "장부 ID", example = "1")
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        log.info("GET /api/v1/books/{} - userId={}", id, userId);
        Book book = bookService.getBook(id, userId);
        BookResponse response = BookMapper.toResponse(book);
        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @Operation(
            summary = "장부 수정",
            description = "장부 이름을 수정합니다. 장부 타입은 변경할 수 없습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 장부에 접근 권한이 없습니다"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "장부를 찾을 수 없습니다"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<BookResponse>> updateBook(
            @Parameter(description = "장부 ID", example = "1")
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody BookUpdateRequest request
    ) {
        log.info("PUT /api/v1/books/{} - userId={}", id, userId);
        Book book = bookService.updateBook(id, userId, request);
        BookResponse response = BookMapper.toResponse(book);
        return ResponseEntity.ok(ResponseFactory.success(response, "장부 수정 완료"));
    }

    @Operation(
            summary = "장부 삭제",
            description = "장부를 비활성화합니다 (Soft Delete). 관련된 거래 데이터는 유지됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 장부에 접근 권한이 없습니다"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "장부를 찾을 수 없습니다"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteBook(
            @Parameter(description = "장부 ID", example = "1")
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        log.info("DELETE /api/v1/books/{} - userId={}", id, userId);
        bookService.deleteBook(id, userId);
        return ResponseEntity.ok(ResponseFactory.successWithMessage("장부 삭제 완료"));
    }
}