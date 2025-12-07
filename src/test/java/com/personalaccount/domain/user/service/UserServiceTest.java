package com.personalaccount.domain.user.service;

import com.personalaccount.domain.user.dto.request.UserCreateRequest;
import com.personalaccount.domain.user.entity.User;
import com.personalaccount.domain.user.repository.UserRepository;
import com.personalaccount.domain.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .password("encodedPassword")
                .name("테스터")
                .isActive(true)
                .build();

        createRequest = UserCreateRequest.builder()
                .email("test@test.com")
                .password("password123")
                .name("테스터")
                .build();
    }

    @Test
    @DisplayName("회원가입_성공_비밀번호암호화확인")
    void createUser_Success() {
        // Given

        // When

        // Then
    }

    @Test
    @DisplayName("회원가입_중복이메일_예외발생")
    void createUser_DuplicateEmail_ThrowsException() {
        // Given

        // When & Then
    }

    @Test
    @DisplayName("사용자조회_성공")
    void getUser_Success() {
        // Given

        // When

        // Then
    }

    @Test
    @DisplayName("사용자조회_존재하지않음_예외발생")
    void getUser_NotFound_ThrowsException() {
        // Given

        // When & Then
    }

    @Test
    @DisplayName("사용자수정_성공")
    void updateUser_Success() {
        // Given

        // When

        // Then
    }

    @Test
    @DisplayName("사용자삭제_SoftDelete_성공")
    void deleteUser_Success() {
        // Given

        // When

        // Then
    }
}