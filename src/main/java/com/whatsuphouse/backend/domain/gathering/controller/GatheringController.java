package com.whatsuphouse.backend.domain.gathering.controller;

import com.whatsuphouse.backend.domain.gathering.dto.GatheringCreateRequest;
import com.whatsuphouse.backend.domain.gathering.dto.GatheringDetailResponse;
import com.whatsuphouse.backend.domain.gathering.dto.GatheringResponse;
import com.whatsuphouse.backend.domain.gathering.dto.GatheringStatusRequest;
import com.whatsuphouse.backend.domain.gathering.dto.GatheringUpdateRequest;
import com.whatsuphouse.backend.domain.gathering.enums.GatheringStatus;
import com.whatsuphouse.backend.domain.gathering.service.GatheringService;
import com.whatsuphouse.backend.global.common.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Tag(name = "모임", description = "모임 API")
@RestController
@RequestMapping("/api/gatherings")
@RequiredArgsConstructor
public class GatheringController {

    private final GatheringService gatheringService;

    @Operation(summary = "모임 목록 조회", description = "날짜 및 상태로 필터링하여 모임 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResult<List<GatheringResponse>>> getGatherings(
            @Parameter(description = "날짜 필터 (YYYY-MM-DD)", example = "2026-04-21")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "상태 필터", example = "OPEN")
            @RequestParam(required = false) GatheringStatus status
    ) {
        return ResponseEntity.ok(ApiResult.success(gatheringService.getGatherings(date, status)));
    }

    @Operation(summary = "모임 상세 조회", description = "모임 상세 정보를 조회합니다. 장소 정보(주소, 지도 URL)를 포함합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<GatheringDetailResponse>> getGathering(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResult.success(gatheringService.getGathering(id)));
    }

    @Operation(summary = "모임 생성 (관리자)", description = "새로운 모임을 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResult<GatheringDetailResponse>> createGathering(
            @Valid @RequestBody GatheringCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success("모임이 등록되었습니다.", gatheringService.createGathering(request)));
    }

    @Operation(summary = "모임 수정 (관리자)", description = "모임 정보를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResult<GatheringDetailResponse>> updateGathering(
            @PathVariable UUID id,
            @Valid @RequestBody GatheringUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResult.success("모임이 수정되었습니다.", gatheringService.updateGathering(id, request)));
    }

    @Operation(summary = "모임 상태 변경 (관리자)", description = "모임 상태를 변경합니다. (OPEN/CLOSED/COMPLETED/CANCELLED)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResult<Void>> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody GatheringStatusRequest request
    ) {
        gatheringService.changeStatus(id, request);
        return ResponseEntity.ok(ApiResult.success("모임 상태가 변경되었습니다.", null));
    }
}
