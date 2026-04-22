package com.whatsuphouse.backend.domain.location.admin.service;

import com.whatsuphouse.backend.domain.location.admin.dto.AdminLocationCreateRequest;
import com.whatsuphouse.backend.domain.location.admin.dto.AdminLocationDetailResponse;
import com.whatsuphouse.backend.domain.location.admin.dto.AdminLocationUpdateRequest;
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
public class AdminLocationService {

    private final LocationRepository locationRepository;

    public AdminLocationDetailResponse createLocation(AdminLocationCreateRequest request) {
        Location location = locationRepository.save(request.toEntity());
        return AdminLocationDetailResponse.from(location);
    }

    public AdminLocationDetailResponse updateLocation(UUID id, AdminLocationUpdateRequest request) {
        Location location = locationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.LOCATION_NOT_FOUND));
        location.update(request.getName(), request.getAddress(), request.getMapUrl(),
                request.getMaxCapacity(), request.getStatus(), request.getMemo());
        return AdminLocationDetailResponse.from(location);
    }
}
