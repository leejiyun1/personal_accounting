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
    public ResponseEntity<CommonResponse<UserResponse>> getUser(
            @PathVariable Long id
    ) {
        log.info("사용자 조회 API 호출: id={}", id);

        // 1. Service 호출
        User user = userService.getUser(id);

        // 2. Entity → DTO 변환
        UserResponse response = UserMapper.toResponse(user);

        // 3. CommonResponse로 감싸서 반환
        return ResponseEntity.ok(
                ResponseFactory.success(response)
        );
    }


    @PostMapping
    public ResponseEntity<CommonResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request
    ) {
        log.info("회원가입 API 호출: email={}", request.getEmail());

        // 1. Service 호출
        User user = userService.createUser(request);

        // 2. Entity → DTO 변환
        UserResponse response = UserMapper.toResponse(user);

        // 3. CommonResponse로 감싸서 반환 (201 Created)
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseFactory.success(response, "회원가입 성공"));
    }


    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        log.info("사용자 수정 API 호출: id={}", id);

        // 1. Service 호출
        User user = userService.updateUser(id, request);

        // 2. Entity → DTO 변환
        UserResponse response = UserMapper.toResponse(user);

        // 3. CommonResponse로 감싸서 반환
        return ResponseEntity.ok(
                ResponseFactory.success(response, "사용자 수정 완료")
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteUser(
            @PathVariable Long id
    ) {
        log.info("사용자 삭제 API 호출: id={}", id);

        // 1. Service 호출
        userService.deleteUser(id);

        // 2. 성공 응답 (데이터 없음)
        return ResponseEntity.ok(
                ResponseFactory.successWithMessage("사용자 삭제 완료")
        );
    }
}