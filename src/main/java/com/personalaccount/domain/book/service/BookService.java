package com.personalaccount.domain.book.service;

import com.personalaccount.domain.book.dto.request.BookCreateRequest;
import com.personalaccount.domain.book.dto.request.BookUpdateRequest;
import com.personalaccount.domain.book.entity.Book;

import java.util.List;

public interface BookService {
    Book createBook(Long userId, BookCreateRequest request);
    List<Book> getBooksByUserId(Long userId);
    Book getBook(Long id, Long userId);
    Book updateBook(Long id, Long userId, BookUpdateRequest request);

    void deleteBook(Long id, Long userId);
}
