package com.whatsuphouse.backend.domain.home.client.service;

import com.whatsuphouse.backend.domain.home.common.dto.response.HomeReviewResponse;
import com.whatsuphouse.backend.domain.home.common.dto.response.HomeReviewsResponse;
import com.whatsuphouse.backend.domain.home.repository.HomeReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HomeReviewService {

    private final HomeReviewRepository homeReviewRepository;

    public HomeReviewsResponse getHomeReviews() {
        List<HomeReviewResponse> reviews = homeReviewRepository.findByIsActiveTrueOrderByDisplayOrderAscCreatedAtDesc()
                .stream()
                .map(HomeReviewResponse::from)
                .toList();

        return HomeReviewsResponse.from(reviews);
    }
}
