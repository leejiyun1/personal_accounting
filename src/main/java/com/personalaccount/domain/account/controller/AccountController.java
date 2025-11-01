package com.personalaccount.domain.account.controller;

import com.personalaccount.domain.account.dto.mapper.AccountMapper;
import com.personalaccount.domain.account.dto.response.AccountResponse;
import com.personalaccount.domain.account.dto.response.CategoryResponse;
import com.personalaccount.domain.account.entity.Account;
import com.personalaccount.domain.account.service.AccountService;
import com.personalaccount.domain.book.entity.BookType;
import com.personalaccount.common.dto.CommonResponse;
import com.personalaccount.common.dto.ResponseFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/categories/income")
    public ResponseEntity<CommonResponse<List<CategoryResponse>>> getIncomeCategories(
            @RequestParam BookType bookType
    ) {
        log.info("수입 카테고리 조회 API 호출: bookType={}", bookType);

        List<Account> accounts = accountService.getIncomeCategories(bookType);
        List<CategoryResponse> response = accounts.stream()
                .map(AccountMapper::toCategoryResponse)
                .toList();

        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @GetMapping("/categories/expense")
    public ResponseEntity<CommonResponse<List<CategoryResponse>>> getExpenseCategories(
            @RequestParam BookType bookType
    ) {
        log.info("지출 카테고리 조회 API 호출: bookType={}", bookType);

        List<Account> accounts = accountService.getExpenseCategories(bookType);
        List<CategoryResponse> response = accounts.stream()
                .map(AccountMapper::toCategoryResponse)
                .toList();

        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @GetMapping("/categories/payment-methods")
    public ResponseEntity<CommonResponse<List<CategoryResponse>>> getPaymentMethods(
            @RequestParam BookType bookType
    ) {
        log.info("결제수단 조회 API 호출: bookType={}", bookType);

        List<Account> accounts = accountService.getPaymentMethods(bookType);
        List<CategoryResponse> response = accounts.stream()
                .map(AccountMapper::toCategoryResponse)
                .toList();

        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @GetMapping("/accounts")
    public ResponseEntity<CommonResponse<List<AccountResponse>>> getAllAccounts(
            @RequestParam BookType bookType
    ) {
        log.info("전체 계정과목 조회 API 호출: bookType={}", bookType);

        List<Account> accounts = accountService.getAllAccounts(bookType);
        List<AccountResponse> response = accounts.stream()
                .map(AccountMapper::toAccountResponse)
                .toList();

        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<CommonResponse<AccountResponse>> getAccount(
            @PathVariable Long id
    ) {
        log.info("계정과목 상세 조회 API 호출: id={}", id);

        Account account = accountService.getAccountById(id);
        AccountResponse response = AccountMapper.toAccountResponse(account);

        return ResponseEntity.ok(ResponseFactory.success(response));
    }
}
