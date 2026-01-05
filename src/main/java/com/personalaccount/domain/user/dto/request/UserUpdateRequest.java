package com.personalaccount.domain.user.dto.request;

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
@Schema(description = "사용자 수정 요청")
public class UserUpdateRequest {

    @Schema(
            description = "이름 (선택적)",
            example = "김철수"
    )
    @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다.")
    private String name;
}