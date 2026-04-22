package com.whatsuphouse.backend.domain.user.client.service;

import com.whatsuphouse.backend.domain.user.client.dto.ProfileResponse;
import com.whatsuphouse.backend.domain.user.client.dto.ProfileUpdateRequest;
import com.whatsuphouse.backend.domain.user.entity.User;
import com.whatsuphouse.backend.domain.user.repository.UserRepository;
import com.whatsuphouse.backend.global.exception.CustomException;
import com.whatsuphouse.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        User user = findActiveUser(userId);
        return ProfileResponse.from(user);
    }

    public ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request) {
        User user = findActiveUser(userId);

        if (request.getNickname() != null
                && !request.getNickname().equals(user.getNickname())
                && userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        user.updateProfile(request.getNickname(), request.getPhone(), request.getName(), request.getGender(), request.getAge());
        return ProfileResponse.from(user);
    }

    @Transactional(readOnly = true)
    public boolean checkNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    @Transactional(readOnly = true)
    public boolean checkEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    public User findActiveUser(UUID userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
