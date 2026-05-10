package com.whatsuphouse.backend.domain.user.admin.service;

import com.whatsuphouse.backend.domain.user.admin.dto.response.UserAdminListResponse;
import com.whatsuphouse.backend.domain.user.admin.dto.response.UserAdminPageResponse;
import com.whatsuphouse.backend.domain.user.entity.User;
import com.whatsuphouse.backend.domain.user.repository.UserRepository;
import com.whatsuphouse.backend.global.exception.CustomException;
import com.whatsuphouse.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    public UserAdminPageResponse listUsers(String search, int page, int size) {
        if (page < 0) {
            throw new CustomException(ErrorCode.INVALID_PAGE_SIZE);
        }
        if (size < 1 || size > 100) {
            throw new CustomException(ErrorCode.INVALID_PAGE_SIZE);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> rawPage = userRepository.findUsersWithApplicationStats(search, pageable);

        Page<UserAdminListResponse> dtoPage = rawPage.map(row ->
                UserAdminListResponse.of((User) row[0], (Number) row[1], (Number) row[2])
        );

        return UserAdminPageResponse.from(dtoPage);
    }
}
