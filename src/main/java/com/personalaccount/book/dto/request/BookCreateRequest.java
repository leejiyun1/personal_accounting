package com.personalaccount.book.dto.request;

import com.personalaccount.book.entity.BookType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookCreateRequest {
    @NotNull(message = "장부 타입은 필수입니다.")
    private BookType bookType;

    @NotBlank(message = "장부 이름은 필수입니다.")
    @Size(max = 100, message = "장부 이름은 100자를 초과할 수 없습니다.")
    private String name;
}
