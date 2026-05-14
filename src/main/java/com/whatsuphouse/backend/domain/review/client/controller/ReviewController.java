package com.whatsuphouse.backend.domain.review.client.controller;

import com.whatsuphouse.backend.domain.review.client.dto.request.ReviewCreateRequest;
import com.whatsuphouse.backend.domain.review.client.dto.response.ReviewPageResponse;
import com.whatsuphouse.backend.domain.review.client.dto.response.ReviewResponse;
import com.whatsuphouse.backend.domain.review.client.service.ReviewService;
import com.whatsuphouse.backend.domain.review.enums.ReviewSort;
import com.whatsuphouse.backend.global.auth.UserPrincipal;
import com.whatsuphouse.backend.global.common.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "리뷰", description = "리뷰 API")
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성", description = "로그인한 회원이 출석 완료된 신청 건에 대해 리뷰를 작성합니다.")
    @PostMapping("/api/reviews")
    public ResponseEntity<ApiResult<ReviewResponse>> createReview(
            @Valid @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success("리뷰가 등록되었습니다.",
                        reviewService.createReview(request, principal.getUserId())));
    }

    @Operation(summary = "전체 리뷰 목록 조회", description = "전체 리뷰를 최신순 또는 추천순으로 조회합니다.")
    @GetMapping("/api/reviews")
    public ResponseEntity<ApiResult<ReviewPageResponse>> getReviews(
            @Parameter(description = "정렬 기준", example = "LATEST")
            @RequestParam(defaultValue = "LATEST") ReviewSort sort,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResult.success(reviewService.getReviews(sort, page, size)));
    }

    @Operation(summary = "게더링별 리뷰 목록 조회", description = "특정 게더링에 작성된 리뷰를 최신순 또는 추천순으로 조회합니다.")
    @GetMapping("/api/gatherings/{gatheringId}/reviews")
    public ResponseEntity<ApiResult<ReviewPageResponse>> getGatheringReviews(
            @PathVariable UUID gatheringId,
            @Parameter(description = "정렬 기준", example = "LATEST")
            @RequestParam(defaultValue = "LATEST") ReviewSort sort,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResult.success(
                reviewService.getGatheringReviews(gatheringId, sort, page, size)));
    }
}
