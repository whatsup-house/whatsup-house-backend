package com.whatsuphouse.backend.domain.mileage.service;

import com.whatsuphouse.backend.domain.mileage.entity.MileageHistory;
import com.whatsuphouse.backend.domain.mileage.enums.MileageType;
import com.whatsuphouse.backend.domain.mileage.repository.MileageHistoryRepository;
import com.whatsuphouse.backend.domain.user.entity.User;
import com.whatsuphouse.backend.global.common.enums.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MileageServiceTest {

    @Mock
    private MileageHistoryRepository mileageHistoryRepository;

    @InjectMocks
    private MileageService mileageService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .email("test@example.com")
                .password("encoded")
                .name("홍길동")
                .gender(Gender.MALE)
                .age(25)
                .nickname("gildong")
                .phone("01012345678")
                .build();
        ReflectionTestUtils.setField(user, "id", userId);
    }

    @Test
    @DisplayName("회원가입 마일리지를 지급하고 이력을 저장한다")
    void rewardSignup_success() {
        given(mileageHistoryRepository.existsByUserAndTypeAndRelatedId(user, MileageType.SIGNUP, userId))
                .willReturn(false);
        given(mileageHistoryRepository.save(any(MileageHistory.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        mileageService.rewardSignup(user);

        ArgumentCaptor<MileageHistory> captor = ArgumentCaptor.forClass(MileageHistory.class);
        verify(mileageHistoryRepository).save(captor.capture());

        MileageHistory history = captor.getValue();
        assertThat(user.getMileageBalance()).isEqualTo(1000);
        assertThat(history.getUser()).isEqualTo(user);
        assertThat(history.getType()).isEqualTo(MileageType.SIGNUP);
        assertThat(history.getAmount()).isEqualTo(1000);
        assertThat(history.getBalanceAfter()).isEqualTo(1000);
        assertThat(history.getRelatedId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("이미 지급된 마일리지는 중복 지급하지 않는다")
    void rewardSignup_duplicate_throwsException() {
        given(mileageHistoryRepository.existsByUserAndTypeAndRelatedId(user, MileageType.SIGNUP, userId))
                .willReturn(true);

        assertThatThrownBy(() -> mileageService.rewardSignup(user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 지급된 마일리지입니다.");

        assertThat(user.getMileageBalance()).isZero();
        verify(mileageHistoryRepository, never()).save(any());
    }
}
