package com.personalaccount.domain.book.dto.mapper;

import com.personalaccount.domain.book.dto.request.BookCreateRequest;
import com.personalaccount.domain.book.dto.response.BookResponse;
import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.user.entity.User;

public class BookMapper {
    public static BookResponse toResponse(Book book) {
        if (book == null) {
            return null;
        }
        return BookResponse.builder()
                .id(book.getId())
                .name(book.getName())
                .bookType(book.getBookType())
                .userId(book.getUser().getId())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    public static Book toEntity(BookCreateRequest request, User user) {
        if (request == null || user == null) {
            return null;
        }
        return Book.builder()
                .name(request.getName())
                .bookType(request.getBookType())
                .user(user)
                .build();
    }
}
