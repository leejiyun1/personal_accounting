package com.personalaccount.account.dto.mapper;

import com.personalaccount.account.dto.response.AccountResponse;
import com.personalaccount.account.dto.response.CategoryResponse;
import com.personalaccount.account.entity.Account;

public class AccountMapper {

    public static CategoryResponse toCategoryResponse(Account account) {
        if (account == null) {
            return null;
        }
        return CategoryResponse.builder()
                .id(account.getId())
                .code(account.getCode())
                .name(account.getName())
                .build();
    }

    public static AccountResponse toAccountResponse(Account account) {
        if (account == null) {
            return null;
        }
        return AccountResponse.builder()
                .id(account.getId())
                .code(account.getCode())
                .name(account.getName())
                .accountType(account.getAccountType())
                .bookType(account.getBookType())
                .build();
    }
}
