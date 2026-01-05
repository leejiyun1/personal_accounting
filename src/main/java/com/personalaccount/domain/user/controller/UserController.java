package com.personalaccount.domain.user.controller;

import com.personalaccount.common.dto.CommonResponse;
import com.personalaccount.common.dto.ResponseFactory;
import com.personalaccount.domain.user.dto.mapper.UserMapper;
import com.personalaccount.domain.user.dto.request.UserCreateRequest;
import com.personalaccount.domain.user.dto.request.UserUpdateRequest;
import com.personalaccount.domain.user.dto.response.UserResponse;
import com.personalaccount.domain.user.entity.User;
import com.personalaccount.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<UserResponse>> getUser(@PathVariable Long id) {
        log.info("GET /api/v1/users/{}", id);
        User user = userService.getUser(id);
        UserResponse response = UserMapper.toResponse(user);
        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @PostMapping
    public ResponseEntity<CommonResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request
    ) {
        log.info("POST /api/v1/users");
        User user = userService.createUser(request);
        UserResponse response = UserMapper.toResponse(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseFactory.success(response, "회원가입 성공"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        log.info("PUT /api/v1/users/{}", id);
        User user = userService.updateUser(id, request);
        UserResponse response = UserMapper.toResponse(user);
        return ResponseEntity.ok(ResponseFactory.success(response, "사용자 수정 완료"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/v1/users/{}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(ResponseFactory.successWithMessage("사용자 삭제 완료"));
    }
}