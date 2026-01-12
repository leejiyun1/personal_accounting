package com.personalaccount.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalaccount.domain.user.dto.mapper.UserMapper;
import com.personalaccount.domain.user.dto.request.UserCreateRequest;
import com.personalaccount.domain.user.dto.request.UserUpdateRequest;
import com.personalaccount.domain.user.dto.response.UserResponse;
import com.personalaccount.domain.user.entity.User;
import com.personalaccount.domain.user.service.UserService;
import com.personalaccount.presentation.UserController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController 테스트")
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("사용자_조회_성공")
    void getUser_Success() throws Exception {
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("테스터")
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .email("test@test.com")
                .name("테스터")
                .build();

        given(userService.getUser(1L)).willReturn(user);
        given(userMapper.toResponse(user)).willReturn(response);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("test@test.com"));

        verify(userService).getUser(1L);
    }

    @Test
    @DisplayName("회원가입_성공")
    void createUser_Success() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .email("new@test.com")
                .password("password123")
                .name("신규유저")
                .build();

        User user = User.builder()
                .id(1L)
                .email("new@test.com")
                .name("신규유저")
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .email("new@test.com")
                .name("신규유저")
                .build();

        given(userService.createUser(any(UserCreateRequest.class))).willReturn(user);
        given(userMapper.toResponse(user)).willReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("new@test.com"));

        verify(userService).createUser(any(UserCreateRequest.class));
    }

    @Test
    @DisplayName("사용자_수정_성공")
    void updateUser_Success() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .name("수정된이름")
                .build();

        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("수정된이름")
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .email("test@test.com")
                .name("수정된이름")
                .build();

        given(userService.updateUser(eq(1L), any(UserUpdateRequest.class))).willReturn(user);
        given(userMapper.toResponse(user)).willReturn(response);

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("수정된이름"));

        verify(userService).updateUser(eq(1L), any(UserUpdateRequest.class));
    }

    @Test
    @DisplayName("사용자_삭제_성공")
    void deleteUser_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(userService).deleteUser(1L);
    }
}