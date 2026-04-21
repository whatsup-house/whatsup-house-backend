package com.whatsuphouse.backend.domain.gathering.controller;

import com.whatsuphouse.backend.domain.gathering.dto.GatheringResponse;
import com.whatsuphouse.backend.domain.gathering.enums.GatheringStatus;
import com.whatsuphouse.backend.domain.gathering.service.GatheringService;
import com.whatsuphouse.backend.global.common.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "모임", description = "모임 목록 조회 API")
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
}
