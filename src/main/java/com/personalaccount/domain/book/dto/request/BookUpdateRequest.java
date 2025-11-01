package com.personalaccount.domain.book.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookUpdateRequest {
    @Size(max = 100, message = "장부 이름은 100자를 초과할 수 없습니다.")
    private String name;
}
