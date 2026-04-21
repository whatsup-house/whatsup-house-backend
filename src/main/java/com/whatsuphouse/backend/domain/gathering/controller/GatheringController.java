package com.whatsuphouse.backend.domain.gathering.controller;

import com.whatsuphouse.backend.domain.gathering.dto.GatheringResponse;
import com.whatsuphouse.backend.domain.gathering.enums.GatheringStatus;
import com.whatsuphouse.backend.domain.gathering.service.GatheringService;
import com.whatsuphouse.backend.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/gatherings")
@RequiredArgsConstructor
public class GatheringController {

    private final GatheringService gatheringService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GatheringResponse>>> getGatherings(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) GatheringStatus status
    ) {
        return ResponseEntity.ok(ApiResponse.success(gatheringService.getGatherings(date, status)));
    }
}
