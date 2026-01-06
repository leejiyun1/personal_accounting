package com.personalaccount.domain.book.dto.mapper;

import com.personalaccount.domain.book.dto.response.BookResponse;
import com.personalaccount.domain.book.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookMapper {

    @Mapping(source = "user.id", target = "userId")
    BookResponse toResponse(Book book);
}