package com.personalaccount.auth.service;

import com.personalaccount.auth.dto.request.LoginRequest;
import com.personalaccount.auth.dto.request.RefreshRequest;
import com.personalaccount.auth.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse refresh(RefreshRequest request);
    void logout(String accessToken);
}