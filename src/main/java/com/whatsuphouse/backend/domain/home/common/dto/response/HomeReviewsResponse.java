package com.whatsuphouse.backend.domain.home.common.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HomeReviewsResponse {

    private List<HomeReviewResponse> reviews;

    public static HomeReviewsResponse from(List<HomeReviewResponse> reviews) {
        return HomeReviewsResponse.builder()
                .reviews(reviews)
                .build();
    }
}
