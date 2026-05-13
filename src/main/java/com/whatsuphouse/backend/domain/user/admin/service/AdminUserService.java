package com.whatsuphouse.backend.domain.user.admin.service;

import com.whatsuphouse.backend.domain.user.admin.dto.response.UserListResponse;
import com.whatsuphouse.backend.domain.user.admin.dto.response.UserPageResponse;
import com.whatsuphouse.backend.domain.user.repository.UserApplicationStatsProjection;
import com.whatsuphouse.backend.domain.user.repository.UserRepository;
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

    public UserPageResponse listUsers(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserApplicationStatsProjection> rawPage = userRepository.findUsersWithApplicationStats(search, pageable);

        Page<UserListResponse> dtoPage = rawPage.map(row ->
                UserListResponse.of(row.user(), row.totalApplications(), row.attendedCount())
        );

        return UserPageResponse.from(dtoPage);
    }
}
