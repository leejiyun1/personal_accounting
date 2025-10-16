package com.personalaccount.transaction.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionUpdateRequest {

    @Size(max = 500, message = "메모는 500자를 초과할 수 없습니다.")
    private String memo;
}