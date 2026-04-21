package com.whatsuphouse.backend.domain.location.service;

import com.whatsuphouse.backend.domain.location.dto.LocationCreateRequest;
import com.whatsuphouse.backend.domain.location.dto.LocationDetailResponse;
import com.whatsuphouse.backend.domain.location.dto.LocationResponse;
import com.whatsuphouse.backend.domain.location.dto.LocationUpdateRequest;
import com.whatsuphouse.backend.domain.location.entity.Location;
import com.whatsuphouse.backend.domain.location.repository.LocationRepository;
import com.whatsuphouse.backend.global.exception.CustomException;
import com.whatsuphouse.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    public List<LocationResponse> getLocations() {
        return locationRepository.findByDeletedAtIsNull()
                .stream().map(LocationResponse::from).toList();
    }

    public LocationDetailResponse getLocation(UUID id) {
        return locationRepository.findByIdAndDeletedAtIsNull(id)
                .map(LocationDetailResponse::from)
                .orElseThrow(() -> new CustomException(ErrorCode.LOCATION_NOT_FOUND));
    }

    @Transactional
    public LocationDetailResponse createLocation(LocationCreateRequest request) {
        Location location = locationRepository.save(request.toEntity());
        return LocationDetailResponse.from(location);
    }

    @Transactional
    public LocationDetailResponse updateLocation(UUID id, LocationUpdateRequest request) {
        Location location = locationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.LOCATION_NOT_FOUND));
        location.update(request.getName(), request.getAddress(), request.getMapUrl(),
                request.getMaxCapacity(), request.getStatus(), request.getMemo());
        return LocationDetailResponse.from(location);
    }
}
