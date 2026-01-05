package com.personalaccount.domain.transaction.dto.request;

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
@Schema(description = "거래 수정 요청 (메모만 수정 가능)")
public class TransactionUpdateRequest {

    @Schema(
            description = "메모 (선택적, 500자 이하)",
            example = "수정된 메모 내용"
    )
    @Size(max = 500, message = "메모는 500자를 초과할 수 없습니다.")
    private String memo;
}