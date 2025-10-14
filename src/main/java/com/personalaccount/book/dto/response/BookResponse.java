package com.personalaccount.book.dto.response;

import com.personalaccount.book.entity.BookType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {
    private Long id;
    private String name;
    private BookType bookType;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
