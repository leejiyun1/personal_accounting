package com.personalaccount.domain.book.dto.request;

import com.personalaccount.domain.book.entity.BookType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "장부 생성 요청")
public class BookCreateRequest {

    @Schema(
            description = "장부 타입 (PERSONAL: 개인, BUSINESS: 사업)",
            example = "PERSONAL",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "장부 타입은 필수입니다.")
    private BookType bookType;

    @Schema(
            description = "장부 이름",
            example = "내 가계부",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "장부 이름은 필수입니다.")
    @Size(max = 100, message = "장부 이름은 100자를 초과할 수 없습니다.")
    private String name;
}