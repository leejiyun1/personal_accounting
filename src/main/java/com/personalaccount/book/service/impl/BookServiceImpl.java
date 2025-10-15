package com.personalaccount.book.service.impl;

import com.personalaccount.book.dto.mapper.BookMapper;
import com.personalaccount.book.dto.request.BookCreateRequest;
import com.personalaccount.book.dto.request.BookUpdateRequest;
import com.personalaccount.book.entity.Book;
import com.personalaccount.book.repository.BookRepository;
import com.personalaccount.book.service.BookService;
import com.personalaccount.common.exception.custom.*;
import com.personalaccount.user.entity.User;
import com.personalaccount.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public Book createBook(Long userId, BookCreateRequest request) {
        log.info("장부 생성 요청: userId={}, bookType={}", userId, request.getBookType());

        // 1. 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 2. 같은 타입의 장부가 이미 있는지 확인
        boolean exists = bookRepository
                .findByUserIdAndBookTypeAndIsActive(userId, request.getBookType(), true)
                .isPresent();

        if (exists) {
            throw new DuplicateBookTypeException(request.getBookType());
        }

        // 3. 장부 생성
        Book book = BookMapper.toEntity(request, user);
        Book savedBook = bookRepository.save(book);

        log.info("장부 생성 완료: bookId={}", savedBook.getId());

        return savedBook;
    }

    @Override
    public List<Book> getBooksByUserId(Long userId) {
        log.debug("장부 목록 조회: userId={}", userId);

        return bookRepository.findByUserIdAndActive(userId, true);
    }

    @Override
    public Book getBook(Long id, Long userId) {
        log.debug("장부 조회: bookId={}, userId={}", id, userId);

        // 1. 장부 조회
        Book book = bookRepository.findByIdAndIsActive(id, true)
                .orElseThrow(() -> new BookNotFoundException(id));

        // 2. 권한 확인 (본인 장부인지)
        if (!book.getUser().getId().equals(userId)){
            throw new UnauthorizedBookAccessException(id);
        }
        return book;
    }

    @Transactional
    @Override
    public Book updateBook(Long id, Long userId, BookUpdateRequest request) {
        log.info("장부 수정 요청: bookId={}, userId={}", id, userId);

        // 1. 장부 조회 및 권한 확인
        Book book = getBook(id, userId);

        // 2. 이름 변경
        if (request.getName() !=null) {
            book.changeName(request.getName());
        }

        log.info("장부 수정 완료: bookId={}", id);

        return book;
    }

    @Transactional
    @Override
    public void deleteBook(Long id, Long userId) {
        log.info("장부 삭제 요청: bookId={}, userId={}", id, userId);

        // 1. 장부 조회 및 권한 확인
        Book book = getBook(id, userId);

        // 2. 비활성화
        book.deactivate();

        log.info("장부 삭제 완료: bookId={}", id);
    }
}
