package com.personalaccount.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommonResponse<T> {

    private Boolean success;
    private T data;
    private String message;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
