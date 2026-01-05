package com.personalaccount.domain.book.dto.response;

import com.personalaccount.domain.book.entity.BookType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "장부 정보 응답")
public class BookResponse {

    @Schema(description = "장부 ID", example = "1")
    private Long id;

    @Schema(description = "장부 이름", example = "내 가계부")
    private String name;

    @Schema(description = "장부 타입", example = "PERSONAL")
    private BookType bookType;

    @Schema(description = "소유자 ID", example = "1")
    private Long userId;

    @Schema(description = "생성일시", example = "2025-01-05T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2025-01-05T15:20:00")
    private LocalDateTime updatedAt;
}