package com.personalaccount.domain.account.repository;

import com.personalaccount.domain.account.entity.Account;
import com.personalaccount.domain.account.entity.AccountType;
import com.personalaccount.domain.book.entity.BookType;
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

    Optional<Account> findByNameAndBookTypeAndIsActive(
            String name,
            BookType bookType,
            Boolean isActive
    );
}
