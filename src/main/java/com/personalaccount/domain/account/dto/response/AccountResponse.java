package com.personalaccount.domain.account.dto.response;

import com.personalaccount.domain.account.entity.AccountType;
import com.personalaccount.domain.book.entity.BookType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {
    private Long id;
    private String code;
    private String name;
    private AccountType accountType;
    private BookType bookType;
}
