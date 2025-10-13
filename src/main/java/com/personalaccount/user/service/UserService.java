package com.personalaccount.user.service;

import com.personalaccount.user.dto.request.UserCreateRequest;
import com.personalaccount.user.dto.request.UserUpdateRequest;
import com.personalaccount.user.entity.User;

/**
 * User Service 인터페이스
 */
public interface UserService {

    User getUser(Long id);
    User createUser(UserCreateRequest request);
    User updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);
}