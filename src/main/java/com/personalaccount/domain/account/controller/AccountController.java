package com.personalaccount.domain.account.controller;

import com.personalaccount.domain.account.dto.mapper.AccountMapper;
import com.personalaccount.domain.account.dto.response.AccountResponse;
import com.personalaccount.domain.account.dto.response.CategoryResponse;
import com.personalaccount.domain.account.entity.Account;
import com.personalaccount.domain.account.service.AccountService;
import com.personalaccount.domain.book.entity.BookType;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Account", description = "계정과목 조회 API")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @Operation(
            summary = "수입 카테고리 조회",
            description = "장부 타입별 수입 카테고리(수익 계정)를 조회합니다."
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
    @GetMapping("/categories/income")
    public ResponseEntity<CommonResponse<List<CategoryResponse>>> getIncomeCategories(
            @Parameter(description = "장부 타입 (PERSONAL/BUSINESS)", example = "PERSONAL")
            @RequestParam BookType bookType
    ) {
        log.info("GET /api/v1/categories/income - bookType={}", bookType);
        List<Account> accounts = accountService.getIncomeCategories(bookType);
        List<CategoryResponse> response = accounts.stream()
                .map(accountMapper::toCategoryResponse)
                .toList();
        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @Operation(
            summary = "지출 카테고리 조회",
            description = "장부 타입별 지출 카테고리(비용 계정)를 조회합니다."
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
    @GetMapping("/categories/expense")
    public ResponseEntity<CommonResponse<List<CategoryResponse>>> getExpenseCategories(
            @Parameter(description = "장부 타입 (PERSONAL/BUSINESS)", example = "PERSONAL")
            @RequestParam BookType bookType
    ) {
        log.info("GET /api/v1/categories/expense - bookType={}", bookType);
        List<Account> accounts = accountService.getExpenseCategories(bookType);
        List<CategoryResponse> response = accounts.stream()
                .map(accountMapper::toCategoryResponse)
                .toList();
        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @Operation(
            summary = "결제수단 조회",
            description = "장부 타입별 결제수단을 조회합니다."
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
    @GetMapping("/categories/payment-methods")
    public ResponseEntity<CommonResponse<List<CategoryResponse>>> getPaymentMethods(
            @Parameter(description = "장부 타입 (PERSONAL/BUSINESS)", example = "PERSONAL")
            @RequestParam BookType bookType
    ) {
        log.info("GET /api/v1/categories/payment-methods - bookType={}", bookType);
        List<Account> accounts = accountService.getPaymentMethods(bookType);
        List<CategoryResponse> response = accounts.stream()
                .map(accountMapper::toCategoryResponse)
                .toList();
        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @Operation(
            summary = "전체 계정과목 조회",
            description = "장부 타입별 모든 활성화된 계정과목을 조회합니다."
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
    @GetMapping("/accounts")
    public ResponseEntity<CommonResponse<List<AccountResponse>>> getAllAccounts(
            @Parameter(description = "장부 타입 (PERSONAL/BUSINESS)", example = "PERSONAL")
            @RequestParam BookType bookType
    ) {
        log.info("GET /api/v1/accounts - bookType={}", bookType);
        List<Account> accounts = accountService.getAllAccounts(bookType);
        List<AccountResponse> response = accounts.stream()
                .map(accountMapper::toAccountResponse)
                .toList();
        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @Operation(
            summary = "계정과목 상세 조회",
            description = "계정과목 ID로 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "계정과목을 찾을 수 없습니다"
            )
    })
    @GetMapping("/accounts/{id}")
    public ResponseEntity<CommonResponse<AccountResponse>> getAccount(
            @Parameter(description = "계정과목 ID", example = "1")
            @PathVariable Long id
    ) {
        log.info("GET /api/v1/accounts/{}", id);
        Account account = accountService.getAccountById(id);
        AccountResponse response = accountMapper.toAccountResponse(account);
        return ResponseEntity.ok(ResponseFactory.success(response));
    }
}