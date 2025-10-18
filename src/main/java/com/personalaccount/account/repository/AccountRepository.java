package com.personalaccount.account.repository;

import com.personalaccount.account.entity.Account;
import com.personalaccount.account.entity.AccountType;
import com.personalaccount.book.entity.BookType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByBookTypeAndAccountTypeAndIsActive(
            BookType bookType,
            AccountType accountType,
            Boolean isActive
    );

    Optional<Account> findByCode(String code);

    List<Account> findByBookTypeAndIsActive(BookType bookType, Boolean isActive);

    Optional<Account> findByNameAndIsActive(String name, Boolean isActive);
}
