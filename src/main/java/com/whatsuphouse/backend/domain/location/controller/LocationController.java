package com.whatsuphouse.backend.domain.location.controller;

import com.whatsuphouse.backend.domain.location.dto.LocationResponse;
import com.whatsuphouse.backend.domain.location.service.LocationService;
import com.whatsuphouse.backend.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LocationResponse>>> getLocations() {
        return ResponseEntity.ok(ApiResponse.success(locationService.getLocations()));
    }
}
