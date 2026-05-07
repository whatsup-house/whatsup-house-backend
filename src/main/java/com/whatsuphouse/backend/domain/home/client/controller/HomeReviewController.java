package com.whatsuphouse.backend.domain.home.client.controller;

import com.whatsuphouse.backend.domain.home.client.service.HomeReviewService;
import com.whatsuphouse.backend.domain.home.common.dto.response.HomeReviewsResponse;
import com.whatsuphouse.backend.global.common.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "홈", description = "홈 화면 API")
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeReviewController {

    private final HomeReviewService homeReviewService;

    @Operation(summary = "홈 후기 미리보기 목록 조회", description = "홈 화면 Reviews Section에 표시할 후기 미리보기 목록을 조회합니다.")
    @GetMapping("/reviews")
    public ResponseEntity<ApiResult<HomeReviewsResponse>> getHomeReviews() {
        return ResponseEntity.ok(ApiResult.success(homeReviewService.getHomeReviews()));
    }
}
