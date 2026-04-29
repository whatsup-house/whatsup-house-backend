package com.whatsuphouse.backend.domain.application.admin.service;

import com.whatsuphouse.backend.domain.application.admin.dto.request.AdminApplicationStatusRequest;
import com.whatsuphouse.backend.domain.application.admin.dto.response.AdminApplicationResponse;
import com.whatsuphouse.backend.domain.application.entity.Application;
import com.whatsuphouse.backend.domain.application.enums.ApplicationStatus;
import com.whatsuphouse.backend.domain.application.repository.ApplicationRepository;
import com.whatsuphouse.backend.domain.gathering.entity.Gathering;
import com.whatsuphouse.backend.global.exception.CustomException;
import com.whatsuphouse.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AdminApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private AdminApplicationService adminApplicationService;

    private UUID gatheringId;
    private UUID applicationId;
    private Gathering gathering;
    private Application application;

    @BeforeEach
    void setUp() {
        gatheringId = UUID.randomUUID();
        applicationId = UUID.randomUUID();

        gathering = Gathering.builder()
                .title("재즈 게더링")
                .eventDate(LocalDate.now().plusDays(7))
                .maxAttendees(10)
                .build();
        ReflectionTestUtils.setField(gathering, "id", gatheringId);

        application = Application.builder()
                .bookingNumber("WH260428-ABC123")
                .gathering(gathering)
                .name("홍길동")
                .phone("01012345678")
                .build();
        ReflectionTestUtils.setField(application, "id", applicationId);
    }

    // ── getAllApplications() ──────────────────────────────────────────────────

    @Test
    @DisplayName("전체 신청 목록 반환")
    void getAllApplications_returnsList() {
        // GIVEN
        given(applicationRepository.findByDeletedAtIsNull()).willReturn(List.of(application));

        // WHEN
        List<AdminApplicationResponse> result = adminApplicationService.getAllApplications();

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    @DisplayName("신청이 없으면 빈 리스트 반환")
    void getAllApplications_empty_returnsEmptyList() {
        // GIVEN
        given(applicationRepository.findByDeletedAtIsNull()).willReturn(List.of());

        // WHEN
        List<AdminApplicationResponse> result = adminApplicationService.getAllApplications();

        // THEN
        assertThat(result).isEmpty();
    }

    // ── getApplicationsByGathering() ─────────────────────────────────────────

    @Test
    @DisplayName("게더링별 신청 목록 반환")
    void getApplicationsByGathering_returnsList() {
        // GIVEN
        given(applicationRepository.findByGatheringIdAndDeletedAtIsNull(gatheringId)).willReturn(List.of(application));

        // WHEN
        List<AdminApplicationResponse> result = adminApplicationService.getApplicationsByGathering(gatheringId);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGatheringId()).isEqualTo(gatheringId);
    }

    @Test
    @DisplayName("해당 게더링에 신청이 없으면 빈 리스트 반환")
    void getApplicationsByGathering_empty_returnsEmptyList() {
        // GIVEN
        given(applicationRepository.findByGatheringIdAndDeletedAtIsNull(gatheringId)).willReturn(List.of());

        // WHEN
        List<AdminApplicationResponse> result = adminApplicationService.getApplicationsByGathering(gatheringId);

        // THEN
        assertThat(result).isEmpty();
    }

    // ── getApplicationsByStatus() ────────────────────────────────────────────

    @Test
    @DisplayName("상태별 신청 목록 반환")
    void getApplicationsByStatus_returnsList() {
        // GIVEN
        given(applicationRepository.findByStatusAndDeletedAtIsNull(ApplicationStatus.PENDING)).willReturn(List.of(application));

        // WHEN
        List<AdminApplicationResponse> result = adminApplicationService.getApplicationsByStatus(ApplicationStatus.PENDING);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    @DisplayName("해당 상태의 신청이 없으면 빈 리스트 반환")
    void getApplicationsByStatus_empty_returnsEmptyList() {
        // GIVEN
        given(applicationRepository.findByStatusAndDeletedAtIsNull(ApplicationStatus.CONFIRMED)).willReturn(List.of());

        // WHEN
        List<AdminApplicationResponse> result = adminApplicationService.getApplicationsByStatus(ApplicationStatus.CONFIRMED);

        // THEN
        assertThat(result).isEmpty();
    }

    // ── getApplication() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("신청 단건 조회 성공")
    void getApplication_success() {
        // GIVEN
        given(applicationRepository.findByIdAndDeletedAtIsNull(applicationId)).willReturn(Optional.of(application));

        // WHEN
        AdminApplicationResponse response = adminApplicationService.getApplication(applicationId);

        // THEN
        assertThat(response.getBookingNumber()).isEqualTo("WH260428-ABC123");
        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    @DisplayName("존재하지 않는 신청 조회 시 예외 발생")
    void getApplication_notFound_throwsException() {
        // GIVEN
        given(applicationRepository.findByIdAndDeletedAtIsNull(applicationId)).willReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> adminApplicationService.getApplication(applicationId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.APPLICATION_NOT_FOUND);
    }

    // ── changeStatus() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("CONFIRMED로 상태 변경 성공")
    void changeStatus_toConfirmed_success() {
        // GIVEN
        given(applicationRepository.findByIdAndDeletedAtIsNull(applicationId)).willReturn(Optional.of(application));
        AdminApplicationStatusRequest request = buildStatusRequest(ApplicationStatus.CONFIRMED);

        // WHEN
        AdminApplicationResponse response = adminApplicationService.changeStatus(applicationId, request);

        // THEN
        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("CANCELLED로 상태 변경 성공")
    void changeStatus_toCancelled_success() {
        // GIVEN
        given(applicationRepository.findByIdAndDeletedAtIsNull(applicationId)).willReturn(Optional.of(application));
        AdminApplicationStatusRequest request = buildStatusRequest(ApplicationStatus.CANCELLED);

        // WHEN
        AdminApplicationResponse response = adminApplicationService.changeStatus(applicationId, request);

        // THEN
        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.CANCELLED);
        assertThat(application.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("ATTENDED로 상태 변경 성공")
    void changeStatus_toAttended_success() {
        // GIVEN
        given(applicationRepository.findByIdAndDeletedAtIsNull(applicationId)).willReturn(Optional.of(application));
        AdminApplicationStatusRequest request = buildStatusRequest(ApplicationStatus.ATTENDED);

        // WHEN
        AdminApplicationResponse response = adminApplicationService.changeStatus(applicationId, request);

        // THEN
        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.ATTENDED);
    }

    @Test
    @DisplayName("PENDING으로 변경 시도 시 예외 발생")
    void changeStatus_toPending_throwsException() {
        // GIVEN
        given(applicationRepository.findByIdAndDeletedAtIsNull(applicationId)).willReturn(Optional.of(application));
        AdminApplicationStatusRequest request = buildStatusRequest(ApplicationStatus.PENDING);

        // WHEN & THEN
        assertThatThrownBy(() -> adminApplicationService.changeStatus(applicationId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CANNOT_CANCEL);
    }

    @Test
    @DisplayName("존재하지 않는 신청 상태 변경 시 예외 발생")
    void changeStatus_notFound_throwsException() {
        // GIVEN
        given(applicationRepository.findByIdAndDeletedAtIsNull(applicationId)).willReturn(Optional.empty());
        AdminApplicationStatusRequest request = buildStatusRequest(ApplicationStatus.CONFIRMED);

        // WHEN & THEN
        assertThatThrownBy(() -> adminApplicationService.changeStatus(applicationId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.APPLICATION_NOT_FOUND);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private AdminApplicationStatusRequest buildStatusRequest(ApplicationStatus status) {
        AdminApplicationStatusRequest request = new AdminApplicationStatusRequest();
        ReflectionTestUtils.setField(request, "status", status);
        return request;
    }
}
