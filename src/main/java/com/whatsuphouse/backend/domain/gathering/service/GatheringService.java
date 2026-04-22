package com.whatsuphouse.backend.domain.gathering.service;

import com.whatsuphouse.backend.domain.gathering.dto.GatheringCreateRequest;
import com.whatsuphouse.backend.domain.gathering.dto.GatheringDetailResponse;
import com.whatsuphouse.backend.domain.gathering.dto.GatheringResponse;
import com.whatsuphouse.backend.domain.gathering.dto.GatheringStatusRequest;
import com.whatsuphouse.backend.domain.gathering.dto.GatheringUpdateRequest;
import com.whatsuphouse.backend.domain.gathering.entity.Gathering;
import com.whatsuphouse.backend.domain.gathering.enums.GatheringStatus;
import com.whatsuphouse.backend.domain.gathering.repository.GatheringRepository;
import com.whatsuphouse.backend.domain.location.entity.Location;
import com.whatsuphouse.backend.domain.location.repository.LocationRepository;
import com.whatsuphouse.backend.global.exception.CustomException;
import com.whatsuphouse.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GatheringService {

    private final GatheringRepository gatheringRepository;
    private final LocationRepository locationRepository;

    public List<GatheringResponse> getGatherings(LocalDate date, GatheringStatus status) {
        if (date != null && status != null) {
            return gatheringRepository.findByEventDateAndStatusAndDeletedAtIsNull(date, status)
                    .stream().map(GatheringResponse::from).toList();
        }
        if (date != null) {
            return gatheringRepository.findByEventDateAndDeletedAtIsNull(date)
                    .stream().map(GatheringResponse::from).toList();
        }
        if (status != null) {
            return gatheringRepository.findByStatusAndDeletedAtIsNull(status)
                    .stream().map(GatheringResponse::from).toList();
        }
        return gatheringRepository.findByDeletedAtIsNull()
                .stream().map(GatheringResponse::from).toList();
    }

    public GatheringDetailResponse getGathering(UUID id) {
        Gathering gathering = gatheringRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.GATHERING_NOT_FOUND));
        return GatheringDetailResponse.from(gathering);
    }

    @Transactional
    public GatheringDetailResponse createGathering(GatheringCreateRequest request) {
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
        return GatheringDetailResponse.from(gatheringRepository.save(gathering));
    }

    @Transactional
    public GatheringDetailResponse updateGathering(UUID id, GatheringUpdateRequest request) {
        Gathering gathering = gatheringRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.GATHERING_NOT_FOUND));
        Location location = locationRepository.findByIdAndDeletedAtIsNull(request.getLocationId())
                .orElseThrow(() -> new CustomException(ErrorCode.LOCATION_NOT_FOUND));
        gathering.update(request.getTitle(), request.getDescription(), location,
                request.getEventDate(), request.getStartTime(), request.getEndTime(),
                request.getPrice(), request.getMaxAttendees(), request.getThumbnailUrl());
        return GatheringDetailResponse.from(gathering);
    }

    @Transactional
    public void changeStatus(UUID id, GatheringStatusRequest request) {
        Gathering gathering = gatheringRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.GATHERING_NOT_FOUND));
        gathering.changeStatus(request.getStatus());
    }
}
