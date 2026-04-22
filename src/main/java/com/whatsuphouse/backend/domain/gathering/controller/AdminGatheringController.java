package com.whatsuphouse.backend.domain.gathering.controller;

import com.whatsuphouse.backend.domain.gathering.dto.GatheringCreateRequest;
import com.whatsuphouse.backend.domain.gathering.dto.GatheringDetailResponse;
import com.whatsuphouse.backend.domain.gathering.dto.GatheringStatusRequest;
import com.whatsuphouse.backend.domain.gathering.dto.GatheringUpdateRequest;
import com.whatsuphouse.backend.domain.gathering.service.GatheringService;
import com.whatsuphouse.backend.global.common.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "모임 관리 (관리자)", description = "관리자 모임 생성, 수정, 상태 변경 API")
@RestController
@RequestMapping("/api/admin/gatherings")
@RequiredArgsConstructor
public class AdminGatheringController {

    private final GatheringService gatheringService;

    @Operation(summary = "모임 생성", description = "관리자 권한이 필요합니다.")
    @PostMapping
    public ResponseEntity<ApiResult<GatheringDetailResponse>> createGathering(
            @Valid @RequestBody GatheringCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success("모임이 등록되었습니다.", gatheringService.createGathering(request)));
    }

    @Operation(summary = "모임 수정", description = "관리자 권한이 필요합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResult<GatheringDetailResponse>> updateGathering(
            @PathVariable UUID id,
            @Valid @RequestBody GatheringUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResult.success("모임이 수정되었습니다.", gatheringService.updateGathering(id, request)));
    }

    @Operation(summary = "모임 상태 변경", description = "관리자 권한이 필요합니다. (OPEN/CLOSED/COMPLETED/CANCELLED)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResult<Void>> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody GatheringStatusRequest request
    ) {
        gatheringService.changeStatus(id, request);
        return ResponseEntity.ok(ApiResult.success("모임 상태가 변경되었습니다.", null));
    }
}
