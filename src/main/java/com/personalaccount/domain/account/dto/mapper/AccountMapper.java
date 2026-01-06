package com.personalaccount.domain.account.dto.mapper;

import com.personalaccount.domain.account.dto.response.AccountResponse;
import com.personalaccount.domain.account.dto.response.CategoryResponse;
import com.personalaccount.domain.account.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {

    CategoryResponse toCategoryResponse(Account account);

    AccountResponse toAccountResponse(Account account);
}