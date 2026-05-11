package com.whatsuphouse.backend.domain.user.admin.service;

import com.whatsuphouse.backend.domain.user.admin.dto.response.UserAdminPageResponse;
import com.whatsuphouse.backend.domain.user.entity.User;
import com.whatsuphouse.backend.domain.user.repository.UserRepository;
import com.whatsuphouse.backend.global.common.enums.Gender;
import com.whatsuphouse.backend.global.exception.CustomException;
import com.whatsuphouse.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@example.com")
                .password("encoded")
                .name("홍길동")
                .gender(Gender.MALE)
                .age(25)
                .nickname("gildong")
                .phone("01012345678")
                .build();
    }

    @Test
    @DisplayName("size가 100을 초과하면 INVALID_PAGE_SIZE 예외 발생")
    void listUsers_throwsWhenSizeExceeds100() {
        assertThatThrownBy(() -> adminUserService.listUsers(null, 0, 101))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PAGE_SIZE);
    }

    @Test
    @DisplayName("size가 0이면 INVALID_PAGE_SIZE 예외 발생")
    void listUsers_throwsWhenSizeIsZero() {
        assertThatThrownBy(() -> adminUserService.listUsers(null, 0, 0))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PAGE_SIZE);
    }

    @Test
    @DisplayName("page가 음수이면 INVALID_PAGE_SIZE 예외 발생")
    void listUsers_throwsWhenPageIsNegative() {
        assertThatThrownBy(() -> adminUserService.listUsers(null, -1, 20))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PAGE_SIZE);
    }

    @Test
    @DisplayName("search가 빈 문자열이면 그대로 전달하여 전체 조회")
    void listUsers_blankSearchPassedThrough() {
        Object[] row = {user, 3L, 2L};
        Page<Object[]> rawPage = new PageImpl<Object[]>(Collections.singletonList(row), PageRequest.of(0, 20), 1);

        given(userRepository.findUsersWithApplicationStats(eq("   "), any())).willReturn(rawPage);

        UserAdminPageResponse result = adminUserService.listUsers("   ", 0, 20);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTotalApplications()).isEqualTo(3L);
        assertThat(result.getContent().get(0).getAttendedCount()).isEqualTo(2L);
        assertThat(result.getContent().get(0).getMileage()).isEqualTo(0);
    }

    @Test
    @DisplayName("정상 조회 시 페이지 정보와 회원 목록 반환")
    void listUsers_returnsPageResponse() {
        Object[] row = {user, 5L, 1L};
        Page<Object[]> rawPage = new PageImpl<Object[]>(Collections.singletonList(row), PageRequest.of(0, 20), 1);

        given(userRepository.findUsersWithApplicationStats(any(), any())).willReturn(rawPage);

        UserAdminPageResponse result = adminUserService.listUsers("gildong", 0, 20);

        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(20);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNickname()).isEqualTo("gildong");
        assertThat(result.getContent().get(0).getMileage()).isEqualTo(0);
    }

    @Test
    @DisplayName("집계값이 null이면 0으로 처리")
    void listUsers_handlesNullAggregations() {
        Object[] row = {user, null, null};
        Page<Object[]> rawPage = new PageImpl<Object[]>(Collections.singletonList(row), PageRequest.of(0, 20), 1);

        given(userRepository.findUsersWithApplicationStats(any(), any())).willReturn(rawPage);

        UserAdminPageResponse result = adminUserService.listUsers(null, 0, 20);

        assertThat(result.getContent().get(0).getTotalApplications()).isEqualTo(0L);
        assertThat(result.getContent().get(0).getAttendedCount()).isEqualTo(0L);
    }
}
