package com.whatsuphouse.backend.domain.user.service;

import com.whatsuphouse.backend.domain.user.dto.ProfileResponse;
import com.whatsuphouse.backend.domain.user.dto.ProfileUpdateRequest;
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

    public User findActiveUser(UUID userId) {
        return userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
