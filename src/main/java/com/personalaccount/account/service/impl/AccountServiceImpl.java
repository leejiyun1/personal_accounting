package com.personalaccount.account.service.impl;

import com.personalaccount.account.entity.Account;
import com.personalaccount.account.entity.AccountType;
import com.personalaccount.account.repository.AccountRepository;
import com.personalaccount.account.service.AccountService;
import com.personalaccount.book.entity.BookType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public List<Account> getIncomeCategories(BookType bookType) {
        log.debug("수입 카테고리 조회: bookType={}", bookType);
        return accountRepository.findByBookTypeAndAccountTypeAndIsActive(
                bookType, AccountType.REVENUE, true
        );
    }

    @Override
    public List<Account> getExpenseCategories(BookType bookType) {
        log.debug("지출 카테고리 조회: bookType={}", bookType);
        return accountRepository.findByBookTypeAndAccountTypeAndIsActive(
                bookType, AccountType.EXPENSE, true
        );
    }

    @Override
    public List<Account> getPaymentMethods(BookType bookType) {
        log.debug("결제수단 조회: bookType={}", bookType);
        return accountRepository.findByBookTypeAndAccountTypeAndIsActive(
                bookType, AccountType.PAYMENT_METHOD, true
        );
    }

    @Override
    public List<Account> getAllAccounts(BookType bookType) {
        log.debug("전체 계정과목 조회: bookType={}", bookType);
        return accountRepository.findByBookTypeAndIsActive(bookType, true);
    }

    @Override
    public Account getAccountById(Long id) {
        log.debug("계정과목 조회: id={}", id);
        return accountRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("계정과목을 찾을 수 없습니다: " + id));
    }
}
