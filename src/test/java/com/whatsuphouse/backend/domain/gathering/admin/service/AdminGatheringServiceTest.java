package com.whatsuphouse.backend.domain.gathering.admin.service;

import com.whatsuphouse.backend.domain.gathering.admin.dto.request.GatheringCreateRequest;
import com.whatsuphouse.backend.domain.gathering.admin.dto.request.GatheringStatusRequest;
import com.whatsuphouse.backend.domain.gathering.admin.dto.request.GatheringUpdateRequest;
import com.whatsuphouse.backend.domain.gathering.common.dto.response.GatheringDetailResponse;
import com.whatsuphouse.backend.domain.gathering.entity.Gathering;
import com.whatsuphouse.backend.domain.gathering.enums.GatheringStatus;
import com.whatsuphouse.backend.domain.gathering.repository.GatheringRepository;
import com.whatsuphouse.backend.domain.location.entity.Location;
import com.whatsuphouse.backend.domain.location.enums.LocationStatus;
import com.whatsuphouse.backend.domain.location.repository.LocationRepository;
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
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AdminGatheringServiceTest {

    @Mock
    private GatheringRepository gatheringRepository;

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private AdminGatheringService adminGatheringService;

    private UUID gatheringId;
    private UUID locationId;
    private Location location;
    private Gathering gathering;

    @BeforeEach
    void setUp() {
        gatheringId = UUID.randomUUID();
        locationId = UUID.randomUUID();

        location = Location.builder()
                .name("재즈바 A")
                .address("서울시 마포구 합정동 123")
                .mapUrl("https://map.kakao.com/link/map/12345678")
                .status(LocationStatus.ACTIVE)
                .maxCapacity(30)
                .build();
        ReflectionTestUtils.setField(location, "id", locationId);

        gathering = Gathering.builder()
                .title("재즈 게더링")
                .description("소규모 재즈 모임")
                .location(location)
                .eventDate(LocalDate.now().plusDays(7))
                .startTime(LocalTime.of(19, 0))
                .endTime(LocalTime.of(21, 0))
                .price(15000)
                .maxAttendees(10)
                .thumbnailUrl("https://example.com/thumb.jpg")
                .build();
        ReflectionTestUtils.setField(gathering, "id", gatheringId);
    }

    // ── createGathering() ────────────────────────────────────────────────────

    @Test
    @DisplayName("게더링 생성 성공")
    void createGathering_success() {
        // GIVEN
        GatheringCreateRequest request = buildCreateRequest("재즈 게더링");
        given(locationRepository.findByIdAndDeletedAtIsNull(locationId)).willReturn(Optional.of(location));
        given(gatheringRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // WHEN
        GatheringDetailResponse response = adminGatheringService.createGathering(request);

        // THEN
        assertThat(response.getTitle()).isEqualTo("재즈 게더링");
        assertThat(response.getStatus()).isEqualTo(GatheringStatus.OPEN);
        assertThat(response.getLocation().getName()).isEqualTo("재즈바 A");
    }

    @Test
    @DisplayName("존재하지 않는 장소로 게더링 생성 시 예외 발생")
    void createGathering_locationNotFound_throwsException() {
        // GIVEN
        GatheringCreateRequest request = buildCreateRequest("재즈 게더링");
        given(locationRepository.findByIdAndDeletedAtIsNull(locationId)).willReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> adminGatheringService.createGathering(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOCATION_NOT_FOUND);
    }

    // ── updateGathering() ────────────────────────────────────────────────────

    @Test
    @DisplayName("게더링 수정 성공")
    void updateGathering_success() {
        // GIVEN
        GatheringUpdateRequest request = buildUpdateRequest("재즈 게더링 (수정)");
        given(gatheringRepository.findByIdAndDeletedAtIsNull(gatheringId)).willReturn(Optional.of(gathering));
        given(locationRepository.findByIdAndDeletedAtIsNull(locationId)).willReturn(Optional.of(location));

        // WHEN
        GatheringDetailResponse response = adminGatheringService.updateGathering(gatheringId, request);

        // THEN
        assertThat(response.getTitle()).isEqualTo("재즈 게더링 (수정)");
    }

    @Test
    @DisplayName("존재하지 않는 게더링 수정 시 예외 발생")
    void updateGathering_gatheringNotFound_throwsException() {
        // GIVEN
        GatheringUpdateRequest request = buildUpdateRequest("재즈 게더링 (수정)");
        given(gatheringRepository.findByIdAndDeletedAtIsNull(gatheringId)).willReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> adminGatheringService.updateGathering(gatheringId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GATHERING_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 장소로 게더링 수정 시 예외 발생")
    void updateGathering_locationNotFound_throwsException() {
        // GIVEN
        GatheringUpdateRequest request = buildUpdateRequest("재즈 게더링 (수정)");
        given(gatheringRepository.findByIdAndDeletedAtIsNull(gatheringId)).willReturn(Optional.of(gathering));
        given(locationRepository.findByIdAndDeletedAtIsNull(locationId)).willReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> adminGatheringService.updateGathering(gatheringId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOCATION_NOT_FOUND);
    }

    // ── changeStatus() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("게더링 상태 CLOSED로 변경 성공")
    void changeStatus_toClosed_success() {
        // GIVEN
        given(gatheringRepository.findByIdAndDeletedAtIsNull(gatheringId)).willReturn(Optional.of(gathering));
        GatheringStatusRequest request = buildStatusRequest(GatheringStatus.CLOSED);

        // WHEN
        adminGatheringService.changeStatus(gatheringId, request);

        // THEN
        assertThat(gathering.getStatus()).isEqualTo(GatheringStatus.CLOSED);
    }

    @Test
    @DisplayName("게더링 상태 COMPLETED로 변경 성공")
    void changeStatus_toCompleted_success() {
        // GIVEN
        given(gatheringRepository.findByIdAndDeletedAtIsNull(gatheringId)).willReturn(Optional.of(gathering));
        GatheringStatusRequest request = buildStatusRequest(GatheringStatus.COMPLETED);

        // WHEN
        adminGatheringService.changeStatus(gatheringId, request);

        // THEN
        assertThat(gathering.getStatus()).isEqualTo(GatheringStatus.COMPLETED);
    }

    @Test
    @DisplayName("게더링 상태 CANCELLED로 변경 성공")
    void changeStatus_toCancelled_success() {
        // GIVEN
        given(gatheringRepository.findByIdAndDeletedAtIsNull(gatheringId)).willReturn(Optional.of(gathering));
        GatheringStatusRequest request = buildStatusRequest(GatheringStatus.CANCELLED);

        // WHEN
        adminGatheringService.changeStatus(gatheringId, request);

        // THEN
        assertThat(gathering.getStatus()).isEqualTo(GatheringStatus.CANCELLED);
    }

    @Test
    @DisplayName("존재하지 않는 게더링 상태 변경 시 예외 발생")
    void changeStatus_notFound_throwsException() {
        // GIVEN
        given(gatheringRepository.findByIdAndDeletedAtIsNull(gatheringId)).willReturn(Optional.empty());
        GatheringStatusRequest request = buildStatusRequest(GatheringStatus.CLOSED);

        // WHEN & THEN
        assertThatThrownBy(() -> adminGatheringService.changeStatus(gatheringId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GATHERING_NOT_FOUND);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private GatheringCreateRequest buildCreateRequest(String title) {
        GatheringCreateRequest request = new GatheringCreateRequest();
        ReflectionTestUtils.setField(request, "title", title);
        ReflectionTestUtils.setField(request, "locationId", locationId);
        ReflectionTestUtils.setField(request, "eventDate", LocalDate.now().plusDays(7));
        ReflectionTestUtils.setField(request, "maxAttendees", 10);
        return request;
    }

    private GatheringUpdateRequest buildUpdateRequest(String title) {
        GatheringUpdateRequest request = new GatheringUpdateRequest();
        ReflectionTestUtils.setField(request, "title", title);
        ReflectionTestUtils.setField(request, "locationId", locationId);
        ReflectionTestUtils.setField(request, "eventDate", LocalDate.now().plusDays(7));
        ReflectionTestUtils.setField(request, "maxAttendees", 10);
        return request;
    }

    private GatheringStatusRequest buildStatusRequest(GatheringStatus status) {
        GatheringStatusRequest request = new GatheringStatusRequest();
        ReflectionTestUtils.setField(request, "status", status);
        return request;
    }
}
