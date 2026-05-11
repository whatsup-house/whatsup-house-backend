package com.whatsuphouse.backend.domain.user.admin.service;

import com.whatsuphouse.backend.domain.user.admin.dto.response.UserAdminListResponse;
import com.whatsuphouse.backend.domain.user.admin.dto.response.UserAdminPageResponse;
import com.whatsuphouse.backend.domain.user.repository.UserApplicationStatsRow;
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

    public UserAdminPageResponse listUsers(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserApplicationStatsRow> rawPage = userRepository.findUsersWithApplicationStats(search, pageable);

        Page<UserAdminListResponse> dtoPage = rawPage.map(row ->
                UserAdminListResponse.of(row.user(), row.totalApplications(), row.attendedCount())
        );

        return UserAdminPageResponse.from(dtoPage);
    }
}
