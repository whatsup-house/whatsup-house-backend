package com.whatsuphouse.backend.domain.gathering.admin.service;

import com.whatsuphouse.backend.domain.gathering.admin.dto.AdminGatheringCreateRequest;
import com.whatsuphouse.backend.domain.gathering.admin.dto.AdminGatheringDetailResponse;
import com.whatsuphouse.backend.domain.gathering.admin.dto.AdminGatheringStatusRequest;
import com.whatsuphouse.backend.domain.gathering.admin.dto.AdminGatheringUpdateRequest;
import com.whatsuphouse.backend.domain.gathering.entity.Gathering;
import com.whatsuphouse.backend.domain.gathering.repository.GatheringRepository;
import com.whatsuphouse.backend.domain.location.entity.Location;
import com.whatsuphouse.backend.domain.location.repository.LocationRepository;
import com.whatsuphouse.backend.global.exception.CustomException;
import com.whatsuphouse.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminGatheringService {

    private final GatheringRepository gatheringRepository;
    private final LocationRepository locationRepository;

    public AdminGatheringDetailResponse createGathering(AdminGatheringCreateRequest request) {
        Location location = locationRepository.findByIdAndDeletedAtIsNull(request.getLocationId())
                .orElseThrow(() -> new CustomException(ErrorCode.LOCATION_NOT_FOUND));
        Gathering gathering = Gathering.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(location)
                .eventDate(request.getEventDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .price(request.getPrice())
                .maxAttendees(request.getMaxAttendees())
                .thumbnailUrl(request.getThumbnailUrl())
                .build();
        return AdminGatheringDetailResponse.from(gatheringRepository.save(gathering));
    }

    public AdminGatheringDetailResponse updateGathering(UUID id, AdminGatheringUpdateRequest request) {
        Gathering gathering = gatheringRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.GATHERING_NOT_FOUND));
        Location location = locationRepository.findByIdAndDeletedAtIsNull(request.getLocationId())
                .orElseThrow(() -> new CustomException(ErrorCode.LOCATION_NOT_FOUND));
        gathering.update(request.getTitle(), request.getDescription(), location,
                request.getEventDate(), request.getStartTime(), request.getEndTime(),
                request.getPrice(), request.getMaxAttendees(), request.getThumbnailUrl());
        return AdminGatheringDetailResponse.from(gathering);
    }

    public void changeStatus(UUID id, AdminGatheringStatusRequest request) {
        Gathering gathering = gatheringRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.GATHERING_NOT_FOUND));
        gathering.changeStatus(request.getStatus());
    }
}
