package com.whatsuphouse.backend.domain.home.common.dto.response;

import com.whatsuphouse.backend.domain.home.entity.HomeReview;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class HomeReviewResponse {

    private UUID id;
    private String content;
    private String authorName;
    private String avatarUrl;
    private String gatheringTitle;
    private Integer rating;

    public static HomeReviewResponse from(HomeReview homeReview) {
        return HomeReviewResponse.builder()
                .id(homeReview.getId())
                .content(homeReview.getContent())
                .authorName(homeReview.getAuthorName())
                .avatarUrl(homeReview.getAvatarUrl())
                .gatheringTitle(homeReview.getGatheringTitle())
                .rating(homeReview.getRating())
                .build();
    }
}
