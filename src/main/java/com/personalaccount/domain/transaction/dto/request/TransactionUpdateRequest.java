package com.personalaccount.domain.transaction.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 거래 수정 요청 DTO
 * - 현재는 메모만 수정 가능
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionUpdateRequest {

    /**
     * 메모 (선택, 최대 500자)
     */
    @Size(max = 500, message = "메모는 500자를 초과할 수 없습니다.")
    private String memo;
}