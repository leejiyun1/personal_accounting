package com.personalaccount.domain.user.service.impl;

import com.personalaccount.common.exception.custom.DuplicateEmailException;
import com.personalaccount.common.exception.custom.UserNotFoundException;
import com.personalaccount.common.util.LogMaskingUtil;
import com.personalaccount.domain.user.dto.mapper.UserMapper;
import com.personalaccount.domain.user.dto.request.UserCreateRequest;
import com.personalaccount.domain.user.dto.request.UserUpdateRequest;
import com.personalaccount.domain.user.entity.User;
import com.personalaccount.domain.user.repository.UserRepository;
import com.personalaccount.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getUser(Long id) {
        log.debug("사용자 조회 요청: id={}", id);

        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    @Override
    public User createUser(UserCreateRequest request) {
        // 마스킹 적용
        log.info("회원가입 요청: email={}", LogMaskingUtil.maskEmail(request.getEmail()));

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        User user = UserMapper.toEntity(request);

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.changePassword(encodedPassword);

        User savedUser = userRepository.save(user);

        // 마스킹 적용
        log.info("회원가입 완료: id={}, email={}",
                savedUser.getId(),
                LogMaskingUtil.maskEmail(savedUser.getEmail()));

        return savedUser;
    }

    @Transactional
    @Override
    public User updateUser(Long id, UserUpdateRequest request) {
        log.info("사용자 수정 요청: id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (request.getName() != null) {
            user.changeName(request.getName());
        }

        log.info("사용자 수정 완료: id={}", id);

        return user;
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        log.info("사용자 삭제 요청: id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userRepository.delete(user);

        log.info("사용자 삭제 완료: id={}", id);
    }
}
