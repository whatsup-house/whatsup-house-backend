package com.whatsuphouse.backend.domain.gathering.admin.service;

import com.whatsuphouse.backend.domain.application.enums.ApplicationStatus;
import com.whatsuphouse.backend.domain.application.repository.ApplicationRepository;
import com.whatsuphouse.backend.domain.application.repository.ApplicationRepository.ApplicationCountProjection;
import com.whatsuphouse.backend.domain.gathering.admin.dto.request.GatheringCreateRequest;
import com.whatsuphouse.backend.domain.gathering.admin.dto.request.GatheringStatusRequest;
import com.whatsuphouse.backend.domain.gathering.admin.dto.request.GatheringUpdateRequest;
import com.whatsuphouse.backend.domain.gathering.admin.dto.response.AdminGatheringResponse;
import com.whatsuphouse.backend.domain.gathering.common.dto.response.GatheringDetailResponse;
import com.whatsuphouse.backend.domain.gathering.entity.Gathering;
import com.whatsuphouse.backend.domain.gathering.enums.GatheringStatus;
import com.whatsuphouse.backend.domain.gathering.repository.GatheringRepository;
import com.whatsuphouse.backend.domain.location.entity.Location;
import com.whatsuphouse.backend.domain.location.repository.LocationRepository;
import com.whatsuphouse.backend.global.exception.CustomException;
import com.whatsuphouse.backend.global.exception.ErrorCode;
import com.whatsuphouse.backend.global.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminGatheringService {

    private final GatheringRepository gatheringRepository;
    private final LocationRepository locationRepository;
    private final ApplicationRepository applicationRepository;
    private final StorageService storageService;

    public List<AdminGatheringResponse> listGatherings(
            GatheringStatus status, LocalDate eventDate, LocalDate from, LocalDate to) {

        List<Gathering> gatherings = resolveGatherings(status, eventDate, from, to);
        if (gatherings.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> gatheringIds = gatherings.stream().map(Gathering::getId).toList();

        Map<UUID, Map<ApplicationStatus, Long>> countMap = applicationRepository
                .countByGatheringIdsGroupByStatus(gatheringIds)
                .stream()
                .collect(Collectors.groupingBy(
                        ApplicationCountProjection::getGatheringId,
                        Collectors.groupingBy(
                                ApplicationCountProjection::getStatus,
                                Collectors.summingLong(ApplicationCountProjection::getCount))));

        return gatherings.stream()
                .map(g -> AdminGatheringResponse.from(g, countMap.getOrDefault(g.getId(), Map.of())))
                .toList();
    }

    private List<Gathering> resolveGatherings(
            GatheringStatus status, LocalDate eventDate, LocalDate from, LocalDate to) {

        if (eventDate != null) {
            return status != null
                    ? gatheringRepository.findByEventDateAndStatusAndDeletedAtIsNull(eventDate, status)
                    : gatheringRepository.findByEventDateAndDeletedAtIsNull(eventDate);
        }
        if (from != null && to != null) {
            return status != null
                    ? gatheringRepository.findByEventDateBetweenAndStatusAndDeletedAtIsNull(from, to, status)
                    : gatheringRepository.findByEventDateBetweenAndDeletedAtIsNull(from, to);
        }
        return status != null
                ? gatheringRepository.findByStatusAndDeletedAtIsNull(status)
                : gatheringRepository.findByDeletedAtIsNull();
    }

    @Transactional
    public GatheringDetailResponse createGathering(GatheringCreateRequest request) {
        Location location = locationRepository.findByIdAndDeletedAtIsNull(request.getLocationId())
                .orElseThrow(() -> new CustomException(ErrorCode.LOCATION_NOT_FOUND));
        // Storage move는 @Transactional 내부에서 호출됨. DB save 실패 시 파일은 롤백 불가.
        // 소규모 어드민 API 특성상 현 구조를 유지하며 trade-off를 허용함 (Carousel과 동일 패턴).
        String thumbnailUrl = StringUtils.hasText(request.getThumbnailTempPath())
                ? storageService.move(request.getThumbnailTempPath(), "gathering")
                : null;
        Gathering gathering = Gathering.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(location)
                .eventDate(request.getEventDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .price(request.getPrice())
                .maxAttendees(request.getMaxAttendees())
                .thumbnailUrl(thumbnailUrl)
                .build();
        return GatheringDetailResponse.from(gatheringRepository.save(gathering));
    }

    @Transactional
    public GatheringDetailResponse updateGathering(UUID id, GatheringUpdateRequest request) {
        Gathering gathering = gatheringRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.GATHERING_NOT_FOUND));
        Location location = locationRepository.findByIdAndDeletedAtIsNull(request.getLocationId())
                .orElseThrow(() -> new CustomException(ErrorCode.LOCATION_NOT_FOUND));
        // Storage move는 @Transactional 내부에서 호출됨. DB save 실패 시 파일은 롤백 불가.
        // 소규모 어드민 API 특성상 현 구조를 유지하며 trade-off를 허용함 (Carousel과 동일 패턴).
        String thumbnailUrl = StringUtils.hasText(request.getThumbnailTempPath())
                ? storageService.move(request.getThumbnailTempPath(), "gathering")
                : gathering.getThumbnailUrl();
        gathering.update(request.getTitle(), request.getDescription(), location,
                request.getEventDate(), request.getStartTime(), request.getEndTime(),
                request.getPrice(), request.getMaxAttendees(), thumbnailUrl);
        return GatheringDetailResponse.from(gathering);
    }

    @Transactional
    public void changeStatus(UUID id, GatheringStatusRequest request) {
        Gathering gathering = gatheringRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.GATHERING_NOT_FOUND));
        gathering.changeStatus(request.getStatus());
    }
}
