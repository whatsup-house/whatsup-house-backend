package com.whatsuphouse.backend.domain.location.service;

import com.whatsuphouse.backend.domain.location.dto.LocationResponse;
import com.whatsuphouse.backend.domain.location.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    public List<LocationResponse> getLocations() {
        return locationRepository.findByDeletedAtIsNull()
                .stream().map(LocationResponse::from).toList();
    }
}
