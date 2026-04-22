package com.whatsuphouse.backend.domain.location.controller;

import com.whatsuphouse.backend.domain.location.dto.LocationDetailResponse;
import com.whatsuphouse.backend.domain.location.dto.LocationResponse;
import com.whatsuphouse.backend.domain.location.service.LocationService;
import com.whatsuphouse.backend.global.common.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "장소", description = "장소 조회 API")
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "장소 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResult<List<LocationResponse>>> getLocations() {
        return ResponseEntity.ok(ApiResult.success(locationService.getLocations()));
    }

    @Operation(summary = "장소 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<LocationDetailResponse>> getLocation(
            @Parameter(description = "장소 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResult.success(locationService.getLocation(id)));
    }
}
