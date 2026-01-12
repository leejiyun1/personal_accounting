package com.personalaccount.application.auth.service;

import com.personalaccount.application.auth.dto.request.LoginRequest;
import com.personalaccount.application.auth.dto.request.RefreshRequest;
import com.personalaccount.application.auth.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse refresh(RefreshRequest request);
    void logout(String accessToken);
}