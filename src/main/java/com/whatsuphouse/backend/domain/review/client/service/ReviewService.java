package com.whatsuphouse.backend.domain.review.client.service;

import com.whatsuphouse.backend.domain.application.entity.Application;
import com.whatsuphouse.backend.domain.application.enums.ApplicationStatus;
import com.whatsuphouse.backend.domain.application.repository.ApplicationRepository;
import com.whatsuphouse.backend.domain.review.client.dto.request.ReviewCreateRequest;
import com.whatsuphouse.backend.domain.review.client.dto.response.ReviewResponse;
import com.whatsuphouse.backend.domain.review.entity.Review;
import com.whatsuphouse.backend.domain.review.entity.ReviewImage;
import com.whatsuphouse.backend.domain.review.enums.ReviewType;
import com.whatsuphouse.backend.domain.review.repository.ReviewImageRepository;
import com.whatsuphouse.backend.domain.review.repository.ReviewRepository;
import com.whatsuphouse.backend.global.exception.CustomException;
import com.whatsuphouse.backend.global.exception.ErrorCode;
import com.whatsuphouse.backend.global.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {

    private final ApplicationRepository applicationRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final StorageService storageService;

    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request, UUID userId) {
        Application application = applicationRepository.findByIdAndDeletedAtIsNull(request.getApplicationId())
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        validateWritableApplication(application, userId);

        if (reviewRepository.existsByApplicationIdAndDeletedAtIsNull(application.getId())) {
            throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        boolean hasImages = request.getImageTempPaths() != null && !request.getImageTempPaths().isEmpty();
        Review review = Review.builder()
                .user(application.getUser())
                .application(application)
                .gathering(application.getGathering())
                .reviewType(hasImages ? ReviewType.PHOTO : ReviewType.TEXT)
                .reviewContent(request.getReviewContent())
                .build();

        Review savedReview = reviewRepository.save(review);
        List<ReviewImage> images = saveImages(savedReview, request.getImageTempPaths());

        return ReviewResponse.of(savedReview, images);
    }

    private void validateWritableApplication(Application application, UUID userId) {
        if (application.getUser() == null || !application.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.REVIEW_APPLICATION_FORBIDDEN);
        }

        if (application.getStatus() != ApplicationStatus.ATTENDED) {
            throw new CustomException(ErrorCode.REVIEW_APPLICATION_NOT_ATTENDED);
        }
    }

    private List<ReviewImage> saveImages(Review review, List<String> imageTempPaths) {
        if (imageTempPaths == null || imageTempPaths.isEmpty()) {
            return List.of();
        }

        List<ReviewImage> images = IntStream.range(0, imageTempPaths.size())
                .mapToObj(index -> {
                    String imageUrl = storageService.move(imageTempPaths.get(index), "review");
                    return ReviewImage.builder()
                            .review(review)
                            .imageUrl(imageUrl)
                            .displayOrder(index)
                            .build();
                })
                .toList();

        return reviewImageRepository.saveAll(images);
    }
}
