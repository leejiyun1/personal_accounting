package com.personalaccount.domain.book.service.impl;

import com.personalaccount.domain.account.constants.DefaultAccounts;
import com.personalaccount.domain.account.constants.DefaultAccounts.AccountTemplate;
import com.personalaccount.domain.account.entity.Account;
import com.personalaccount.domain.account.repository.AccountRepository;
import com.personalaccount.domain.book.dto.mapper.BookMapper;
import com.personalaccount.domain.book.dto.request.BookCreateRequest;
import com.personalaccount.domain.book.dto.request.BookUpdateRequest;
import com.personalaccount.domain.book.entity.Book;
import com.personalaccount.domain.book.repository.BookRepository;
import com.personalaccount.domain.book.service.BookService;
import com.personalaccount.common.exception.custom.*;
import com.personalaccount.domain.user.entity.User;
import com.personalaccount.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Transactional
    @Override
    public Book createBook(Long userId, BookCreateRequest request) {
        log.info("장부 생성 요청: userId={}, bookType={}", userId, request.getBookType());

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        boolean exists = bookRepository
                .findByUserIdAndBookTypeAndIsActive(userId, request.getBookType(), true)
                .isPresent();

        if (exists) {
            throw new DuplicateBookTypeException(request.getBookType());
        }

        User user = userRepository.getReferenceById(userId);

        Book book = BookMapper.toEntity(request, user);
        Book savedBook = bookRepository.save(book);

        createDefaultAccounts(savedBook);

        log.info("장부 생성 완료: bookId={}, 기본 계정과목 생성 완료", savedBook.getId());

        return savedBook;
    }

    private void createDefaultAccounts(Book book) {
        AccountTemplate[] templates = DefaultAccounts.getDefaultAccounts(book.getBookType());

        List<Account> accounts = new ArrayList<>();
        for (AccountTemplate template : templates) {
            Account account = Account.builder()
                    .code(template.code)
                    .name(template.name)
                    .accountType(template.accountType)
                    .bookType(book.getBookType())
                    .build();
            accounts.add(account);
        }

        accountRepository.saveAll(accounts);
        log.debug("기본 계정과목 {}개 생성 완료", accounts.size());
    }

    @Override
    public List<Book> getBooksByUserId(Long userId) {
        log.debug("장부 목록 조회: userId={}", userId);

        return bookRepository.findByUserIdAndIsActive(userId, true);
    }

    @Override
    public Book getBook(Long id, Long userId) {
        log.debug("장부 조회: bookId={}, userId={}", id, userId);

        Book book = bookRepository.findByIdAndIsActive(id, true)
                .orElseThrow(() -> new BookNotFoundException(id));

        if (!book.getUser().getId().equals(userId)) {
            throw new UnauthorizedBookAccessException(id);
        }
        return book;
    }

    @Transactional
    @Override
    public Book updateBook(Long id, Long userId, BookUpdateRequest request) {
        log.info("장부 수정 요청: bookId={}, userId={}", id, userId);

        Book book = getBook(id, userId);

        if (request.getName() != null) {
            book.changeName(request.getName());
        }

        log.info("장부 수정 완료: bookId={}", id);

        return book;
    }

    @Transactional
    @Override
    public void deleteBook(Long id, Long userId) {
        log.info("장부 삭제 요청: bookId={}, userId={}", id, userId);

        Book book = getBook(id, userId);

        book.deactivate();

        log.info("장부 삭제 완료: bookId={}", id);
    }
}