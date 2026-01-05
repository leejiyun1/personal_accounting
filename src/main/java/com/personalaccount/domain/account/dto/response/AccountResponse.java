package com.personalaccount.domain.account.dto.response;

import com.personalaccount.domain.account.entity.AccountType;
import com.personalaccount.domain.book.entity.BookType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "계정과목 정보 (상세)")
public class AccountResponse {

    @Schema(description = "계정과목 ID", example = "1")
    private Long id;

    @Schema(description = "계정 코드", example = "5100")
    private String code;

    @Schema(description = "계정 이름", example = "식비")
    private String name;

    @Schema(description = "계정 타입", example = "EXPENSE")
    private AccountType accountType;

    @Schema(description = "장부 타입", example = "PERSONAL")
    private BookType bookType;
}