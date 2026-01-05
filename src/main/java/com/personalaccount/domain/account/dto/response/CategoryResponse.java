package com.personalaccount.domain.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "카테고리 정보 (간략)")
public class CategoryResponse {

    @Schema(description = "계정과목 ID", example = "1")
    private Long id;

    @Schema(description = "계정 코드", example = "5100")
    private String code;

    @Schema(description = "계정 이름", example = "식비")
    private String name;
}