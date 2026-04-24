package com.whatsuphouse.backend.domain.location.client.service;

import com.whatsuphouse.backend.domain.location.common.dto.response.LocationDetailResponse;
import com.whatsuphouse.backend.domain.location.common.dto.response.LocationResponse;
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
}
