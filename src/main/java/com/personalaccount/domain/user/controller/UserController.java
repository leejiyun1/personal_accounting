package com.personalaccount.domain.user.controller;

import com.personalaccount.common.dto.CommonResponse;
import com.personalaccount.common.dto.ResponseFactory;
import com.personalaccount.domain.user.dto.mapper.UserMapper;
import com.personalaccount.domain.user.dto.request.UserCreateRequest;
import com.personalaccount.domain.user.dto.request.UserUpdateRequest;
import com.personalaccount.domain.user.dto.response.UserResponse;
import com.personalaccount.domain.user.entity.User;
import com.personalaccount.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User", description = "사용자 관리 API")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "사용자 조회",
            description = "사용자 ID로 사용자 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없습니다"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<UserResponse>> getUser(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long id
    ) {
        log.info("GET /api/v1/users/{}", id);
        User user = userService.getUser(id);
        UserResponse response = UserMapper.toResponse(user);
        return ResponseEntity.ok(ResponseFactory.success(response));
    }

    @Operation(
            summary = "회원가입",
            description = "새로운 사용자를 생성합니다. 비밀번호는 BCrypt로 암호화되어 저장됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미 사용 중인 이메일입니다"
            )
    })
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

    @Operation(
            summary = "사용자 수정",
            description = "사용자 정보를 수정합니다. 현재는 이름만 수정 가능합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없습니다"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<UserResponse>> updateUser(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        log.info("PUT /api/v1/users/{}", id);
        User user = userService.updateUser(id, request);
        UserResponse response = UserMapper.toResponse(user);
        return ResponseEntity.ok(ResponseFactory.success(response, "사용자 수정 완료"));
    }

    @Operation(
            summary = "사용자 삭제",
            description = "사용자를 비활성화합니다 (Soft Delete). 실제 데이터는 삭제되지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없습니다"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteUser(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long id
    ) {
        log.info("DELETE /api/v1/users/{}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(ResponseFactory.successWithMessage("사용자 삭제 완료"));
    }
}