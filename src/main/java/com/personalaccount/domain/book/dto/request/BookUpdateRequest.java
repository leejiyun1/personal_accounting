package com.personalaccount.domain.book.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "장부 수정 요청")
public class BookUpdateRequest {

    @Schema(
            description = "장부 이름 (선택적)",
            example = "2025년 가계부"
    )
    @Size(max = 100, message = "장부 이름은 100자를 초과할 수 없습니다.")
    private String name;
}