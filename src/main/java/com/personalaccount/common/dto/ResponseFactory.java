package com.personalaccount.common.dto;

/**
 * CommonResponse 생성을 쉽게 해주는 Factory 클래스
 *
 * Factory Pattern 적용:
 * - 객체 생성 로직을 캡슐화
 * - 코드 중복 제거
 * - 일관된 방식으로 응답 생성
 */
public class ResponseFactory {

    /**
     * 성공 응답 (데이터 있음)
     */
    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.<T>builder()
                .success(true)
                .data(data)
                .message(null)
                .build();
    }

    /**
     * 성공 응답 (메시지 포함)
     */
    public static <T> CommonResponse<T> success(T data, String message) {
        return CommonResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    /**
     * 성공 응답 (데이터 없음, 메시지만)
     * 삭제 같은 경우 사용
     */
    public static <Void> CommonResponse<Void> successWithMessage(String message) {
        return CommonResponse.<Void>builder()
                .success(true)
                .data(null)
                .message(message)
                .build();
    }

    /**
     * 실패 응답 (에러 메시지)
     */
    public static <T> CommonResponse<T> error(String message) {
        return CommonResponse.<T>builder()
                .success(false)
                .data(null)
                .message(message)
                .build();
    }
}