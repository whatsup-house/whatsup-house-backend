package com.whatsuphouse.backend.domain.review.client.controller;

import com.whatsuphouse.backend.domain.review.client.dto.request.ReviewCreateRequest;
import com.whatsuphouse.backend.domain.review.client.dto.response.ReviewResponse;
import com.whatsuphouse.backend.domain.review.client.service.ReviewService;
import com.whatsuphouse.backend.global.auth.UserPrincipal;
import com.whatsuphouse.backend.global.common.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "리뷰", description = "리뷰 API")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성", description = "로그인한 회원이 출석 완료된 신청 건에 대해 리뷰를 작성합니다.")
    @PostMapping
    public ResponseEntity<ApiResult<ReviewResponse>> createReview(
            @Valid @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success("리뷰가 등록되었습니다.",
                        reviewService.createReview(request, principal.getUserId())));
    }
}
