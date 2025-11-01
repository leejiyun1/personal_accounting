package com.personalaccount.domain.account.service;

import com.personalaccount.domain.account.entity.Account;
import com.personalaccount.domain.book.entity.BookType;

import java.util.List;

public interface AccountService {
    List<Account> getIncomeCategories(BookType bookType);
    List<Account> getExpenseCategories(BookType bookType);
    List<Account> getPaymentMethods(BookType bookType);
    List<Account> getAllAccounts(BookType bookType);
    Account getAccountById(Long id);
}
