package com.personalaccount.user.service.impl;

import com.personalaccount.common.exception.custom.DuplicateEmailException;
import com.personalaccount.common.exception.custom.UserNotFoundException;
import com.personalaccount.user.dto.mapper.UserMapper;
import com.personalaccount.user.dto.request.UserCreateRequest;
import com.personalaccount.user.dto.request.UserUpdateRequest;
import com.personalaccount.user.entity.User;
import com.personalaccount.user.repository.UserRepository;
import com.personalaccount.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getUser(Long id) {
        log.debug("사용자 조회 요청: id={}", id);

        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * 사용자 생성 (회원가입)
     *
     * 트랜잭션:
     * - @Transactional (쓰기 가능)
     * - 예외 발생 시 자동 롤백
     *
     * 처리 순서:
     * 1. 이메일 중복 체크
     * 2. 비밀번호 암호화 (TODO: 나중에 BCrypt 적용)
     * 3. User 엔티티 생성
     * 4. DB 저장
     */
    @Transactional
    @Override
    public User createUser(UserCreateRequest request) {
        log.info("회원가입 요청: email={}", request.getEmail());

        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        // 2. DTO → Entity 변환
        User user = UserMapper.toEntity(request);

        // 3. 비밀번호 암호화 (TODO: BCrypt 적용 예정)
        // String encodedPassword = passwordEncoder.encode(user.getPassword());
        // user.changePassword(encodedPassword);

        // 4. DB 저장
        User savedUser = userRepository.save(user);

        log.info("회원가입 완료: id={}, email={}", savedUser.getId(), savedUser.getEmail());

        return savedUser;
    }

    /**
     * 사용자 수정
     *
     * 트랜잭션:
     * - @Transactional (쓰기 가능)
     * - 변경 감지(Dirty Checking)로 자동 UPDATE
     *
     * 처리 순서:
     * 1. 사용자 조회
     * 2. 이름 변경 (비즈니스 메서드 사용)
     * 3. 트랜잭션 커밋 시 자동 UPDATE
     */
    @Transactional
    @Override
    public User updateUser(Long id, UserUpdateRequest request) {
        log.info("사용자 수정 요청: id={}", id);

        // 1. 사용자 조회
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // 2. 이름 변경 (Dirty Checking으로 자동 UPDATE)
        if (request.getName() != null) {
            user.changeName(request.getName());
        }

        log.info("사용자 수정 완료: id={}", id);

        return user;
    }

    /**
     * 사용자 삭제 (탈퇴)
     *
     * 트랜잭션:
     * - @Transactional (쓰기 가능)
     *
     * 처리 방식:
     * - Hard Delete (실제 삭제)
     * - Soft Delete는 나중에 고려 (isActive = false)
     */
    @Transactional
    @Override
    public void deleteUser(Long id) {
        log.info("사용자 삭제 요청: id={}", id);

        // 1. 사용자 조회
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // 2. 삭제
        userRepository.delete(user);

        log.info("사용자 삭제 완료: id={}", id);
    }
}