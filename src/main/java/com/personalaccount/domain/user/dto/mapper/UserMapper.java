package com.personalaccount.domain.user.dto.mapper;

import com.personalaccount.domain.user.dto.request.UserCreateRequest;
import com.personalaccount.domain.user.dto.response.UserResponse;
import com.personalaccount.domain.user.entity.User;


public class UserMapper {
    public static UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public static User toEntity(UserCreateRequest request) {
        if (request == null) {
            return null;
        }

        return User.builder()
                .email(request.getEmail())
                .password(request.getPassword())  // 평문 (Service에서 암호화 필요)
                .name(request.getName())
                .build();
    }
}