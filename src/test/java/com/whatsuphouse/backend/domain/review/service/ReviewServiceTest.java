package com.whatsuphouse.backend.domain.review.service;

import com.whatsuphouse.backend.domain.application.entity.Application;
import com.whatsuphouse.backend.domain.application.enums.ApplicationStatus;
import com.whatsuphouse.backend.domain.application.repository.ApplicationRepository;
import com.whatsuphouse.backend.domain.gathering.entity.Gathering;
import com.whatsuphouse.backend.domain.gathering.repository.GatheringRepository;
import com.whatsuphouse.backend.domain.review.client.dto.request.ReviewCreateRequest;
import com.whatsuphouse.backend.domain.review.client.dto.response.HomeReviewResponse;
import com.whatsuphouse.backend.domain.review.client.dto.response.ReviewDeleteResponse;
import com.whatsuphouse.backend.domain.review.client.dto.response.ReviewLikeResponse;
import com.whatsuphouse.backend.domain.review.client.dto.response.ReviewPageResponse;
import com.whatsuphouse.backend.domain.review.client.dto.response.ReviewResponse;
import com.whatsuphouse.backend.domain.review.client.service.ReviewService;
import com.whatsuphouse.backend.domain.review.entity.Review;
import com.whatsuphouse.backend.domain.review.entity.ReviewImage;
import com.whatsuphouse.backend.domain.review.entity.ReviewLike;
import com.whatsuphouse.backend.domain.review.enums.ReviewSort;
import com.whatsuphouse.backend.domain.review.enums.ReviewType;
import com.whatsuphouse.backend.domain.review.repository.ReviewImageRepository;
import com.whatsuphouse.backend.domain.review.repository.ReviewLikeRepository;
import com.whatsuphouse.backend.domain.review.repository.ReviewRepository;
import com.whatsuphouse.backend.domain.user.entity.User;
import com.whatsuphouse.backend.domain.user.repository.UserRepository;
import com.whatsuphouse.backend.global.common.enums.Gender;
import com.whatsuphouse.backend.global.exception.CustomException;
import com.whatsuphouse.backend.global.exception.ErrorCode;
import com.whatsuphouse.backend.global.storage.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private GatheringRepository gatheringRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewImageRepository reviewImageRepository;

    @Mock
    private ReviewLikeRepository reviewLikeRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private ReviewService reviewService;

    private UUID userId;
    private UUID applicationId;
    private User user;
    private Gathering gathering;
    private Application application;
    private ReviewCreateRequest request;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        applicationId = UUID.randomUUID();

        user = User.builder()
                .email("reviewer@example.com")
                .password("encoded")
                .name("김리뷰")
                .gender(Gender.FEMALE)
                .age(27)
                .nickname("reviewer")
                .phone("01012345678")
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        gathering = Gathering.builder()
                .title("재즈 게더링")
                .eventDate(LocalDate.now().minusDays(1))
                .maxAttendees(10)
                .build();
        ReflectionTestUtils.setField(gathering, "id", UUID.randomUUID());

        application = Application.builder()
                .bookingNumber("WH260514-ABC123")
                .gathering(gathering)
                .user(user)
                .name(user.getName())
                .phone(user.getPhone())
                .build();
        ReflectionTestUtils.setField(application, "id", applicationId);
        application.attend();

        request = new ReviewCreateRequest();
        ReflectionTestUtils.setField(request, "applicationId", applicationId);
        ReflectionTestUtils.setField(request, "reviewContent", "모임 분위기가 정말 좋았고 다시 참여하고 싶어요.");
    }

    @Test
    @DisplayName("이미지 없이 리뷰 작성 성공")
    void createReview_withoutImages_success() {
        given(applicationRepository.findByIdAndDeletedAtIsNull(applicationId)).willReturn(Optional.of(application));
        given(reviewRepository.existsByApplicationIdAndDeletedAtIsNull(applicationId)).willReturn(false);
        given(reviewRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        ReviewResponse response = reviewService.createReview(request, userId);

        assertThat(response.getReviewType()).isEqualTo(ReviewType.TEXT);
        assertThat(response.getReviewContent()).isEqualTo("모임 분위기가 정말 좋았고 다시 참여하고 싶어요.");
        assertThat(response.getImages()).isEmpty();
        assertThat(response.getLikeCount()).isZero();
    }

    @Test
    @DisplayName("이미지가 있으면 PHOTO 리뷰로 작성하고 이미지 순서를 저장")
    void createReview_withImages_success() {
        ReflectionTestUtils.setField(request, "imageTempPaths",
                List.of("temp/review/image-1.jpg", "temp/review/image-2.jpg"));

        given(applicationRepository.findByIdAndDeletedAtIsNull(applicationId)).willReturn(Optional.of(application));
        given(reviewRepository.existsByApplicationIdAndDeletedAtIsNull(applicationId)).willReturn(false);
        given(reviewRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(storageService.move("temp/review/image-1.jpg", "review")).willReturn("https://cdn.example.com/review/image-1.jpg");
        given(storageService.move("temp/review/image-2.jpg", "review")).willReturn("https://cdn.example.com/review/image-2.jpg");
        given(reviewImageRepository.saveAll(any())).willAnswer(inv -> inv.getArgument(0));

        ReviewResponse response = reviewService.createReview(request, userId);

        assertThat(response.getReviewType()).isEqualTo(ReviewType.PHOTO);
        assertThat(response.getImages()).hasSize(2);
        assertThat(response.getImages().get(0).getImageUrl()).isEqualTo("https://cdn.example.com/review/image-1.jpg");
        assertThat(response.getImages().get(0).getDisplayOrder()).isZero();
        assertThat(response.getImages().get(1).getDisplayOrder()).isEqualTo(1);
        verify(reviewImageRepository).saveAll(any());
    }

    @Test
    @DisplayName("신청이 존재하지 않으면 예외 발생")
    void createReview_applicationNotFound_throwsException() {
        given(applicationRepository.findByIdAndDeletedAtIsNull(applicationId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(request, userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.APPLICATION_NOT_FOUND);
    }

    @Test
    @DisplayName("본인 신청이 아니면 예외 발생")
    void createReview_forbidden_throwsException() {
        given(applicationRepository.findByIdAndDeletedAtIsNull(applicationId)).willReturn(Optional.of(application));

        assertThatThrownBy(() -> reviewService.createReview(request, UUID.randomUUID()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_APPLICATION_FORBIDDEN);
    }

    @Test
    @DisplayName("출석 완료 상태가 아니면 예외 발생")
    void createReview_notAttended_throwsException() {
        Application pendingApplication = Application.builder()
                .bookingNumber("WH260514-PENDING")
                .gathering(gathering)
                .user(user)
                .name(user.getName())
                .phone(user.getPhone())
                .build();
        ReflectionTestUtils.setField(pendingApplication, "id", applicationId);
        ReflectionTestUtils.setField(pendingApplication, "status", ApplicationStatus.CONFIRMED);

        given(applicationRepository.findByIdAndDeletedAtIsNull(applicationId)).willReturn(Optional.of(pendingApplication));

        assertThatThrownBy(() -> reviewService.createReview(request, userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_APPLICATION_NOT_ATTENDED);
    }

    @Test
    @DisplayName("이미 리뷰를 작성한 신청이면 예외 발생")
    void createReview_alreadyExists_throwsException() {
        given(applicationRepository.findByIdAndDeletedAtIsNull(applicationId)).willReturn(Optional.of(application));
        given(reviewRepository.existsByApplicationIdAndDeletedAtIsNull(applicationId)).willReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(request, userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("게더링별 리뷰 목록을 최신순으로 조회")
    void getGatheringReviews_latest_success() {
        UUID gatheringId = gathering.getId();
        Review review = buildReview(UUID.randomUUID(), "최신 리뷰입니다.");
        ReviewImage image = ReviewImage.builder()
                .review(review)
                .imageUrl("https://cdn.example.com/review/image.jpg")
                .displayOrder(0)
                .build();
        ReflectionTestUtils.setField(image, "id", UUID.randomUUID());

        given(gatheringRepository.existsByIdAndDeletedAtIsNull(gatheringId)).willReturn(true);
        given(reviewRepository.findByGatheringIdAndDeletedAtIsNull(eq(gatheringId), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(review)));
        given(reviewImageRepository.findByReviewIdInAndDeletedAtIsNullOrderByDisplayOrderAsc(List.of(review.getId())))
                .willReturn(List.of(image));

        ReviewPageResponse response = reviewService.getGatheringReviews(gatheringId, ReviewSort.LATEST, 0, 10);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getReviewContent()).isEqualTo("최신 리뷰입니다.");
        assertThat(response.getContent().get(0).getImages()).hasSize(1);
        assertThat(response.getPage()).isZero();
    }

    @Test
    @DisplayName("게더링별 리뷰 목록을 추천순으로 조회")
    void getGatheringReviews_likes_success() {
        UUID gatheringId = gathering.getId();
        Review review = buildReview(UUID.randomUUID(), "추천순 리뷰입니다.");

        given(gatheringRepository.existsByIdAndDeletedAtIsNull(gatheringId)).willReturn(true);
        given(reviewRepository.findByGatheringIdAndDeletedAtIsNull(eq(gatheringId), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(review)));
        given(reviewImageRepository.findByReviewIdInAndDeletedAtIsNullOrderByDisplayOrderAsc(List.of(review.getId())))
                .willReturn(List.of());

        ReviewPageResponse response = reviewService.getGatheringReviews(gatheringId, ReviewSort.LIKES, 0, 10);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getReviewContent()).isEqualTo("추천순 리뷰입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 게더링의 리뷰 목록 조회 시 예외 발생")
    void getGatheringReviews_gatheringNotFound_throwsException() {
        UUID gatheringId = UUID.randomUUID();
        given(gatheringRepository.existsByIdAndDeletedAtIsNull(gatheringId)).willReturn(false);

        assertThatThrownBy(() -> reviewService.getGatheringReviews(gatheringId, ReviewSort.LATEST, 0, 10))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GATHERING_NOT_FOUND);
    }

    @Test
    @DisplayName("전체 리뷰 목록을 최신순으로 조회")
    void getReviews_latest_success() {
        Review review = buildReview(UUID.randomUUID(), "전체 최신 리뷰입니다.");

        given(reviewRepository.findByDeletedAtIsNull(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(review)));
        given(reviewImageRepository.findByReviewIdInAndDeletedAtIsNullOrderByDisplayOrderAsc(List.of(review.getId())))
                .willReturn(List.of());

        ReviewPageResponse response = reviewService.getReviews(ReviewSort.LATEST, 0, 10);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getReviewContent()).isEqualTo("전체 최신 리뷰입니다.");
        assertThat(response.getPage()).isZero();
    }

    @Test
    @DisplayName("전체 리뷰 목록을 추천순으로 조회")
    void getReviews_likes_success() {
        Review review = buildReview(UUID.randomUUID(), "전체 추천순 리뷰입니다.");

        given(reviewRepository.findByDeletedAtIsNull(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(review)));
        given(reviewImageRepository.findByReviewIdInAndDeletedAtIsNullOrderByDisplayOrderAsc(List.of(review.getId())))
                .willReturn(List.of());

        ReviewPageResponse response = reviewService.getReviews(ReviewSort.LIKES, 0, 10);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getReviewContent()).isEqualTo("전체 추천순 리뷰입니다.");
    }

    @Test
    @DisplayName("리뷰를 추천하면 추천 이력이 생성되고 추천 수가 증가한다")
    void toggleLike_like_success() {
        UUID reviewId = UUID.randomUUID();
        Review review = buildReview(reviewId, "추천할 리뷰입니다.");

        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.of(review));
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
        given(reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId)).willReturn(Optional.empty());
        given(reviewLikeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        ReviewLikeResponse response = reviewService.toggleLike(reviewId, userId);

        assertThat(response.isLiked()).isTrue();
        assertThat(response.getLikeCount()).isEqualTo(1);
        verify(reviewLikeRepository).save(any(ReviewLike.class));
    }

    @Test
    @DisplayName("이미 추천한 리뷰를 다시 요청하면 추천이 취소되고 추천 수가 감소한다")
    void toggleLike_unlike_success() {
        UUID reviewId = UUID.randomUUID();
        Review review = buildReview(reviewId, "추천 취소할 리뷰입니다.");
        review.increaseLikeCount();
        ReviewLike reviewLike = ReviewLike.builder()
                .review(review)
                .user(user)
                .build();

        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.of(review));
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
        given(reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId)).willReturn(Optional.of(reviewLike));

        ReviewLikeResponse response = reviewService.toggleLike(reviewId, userId);

        assertThat(response.isLiked()).isFalse();
        assertThat(response.getLikeCount()).isZero();
        verify(reviewLikeRepository).delete(reviewLike);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰 추천 시 예외 발생")
    void toggleLike_reviewNotFound_throwsException() {
        UUID reviewId = UUID.randomUUID();
        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.toggleLike(reviewId, userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 리뷰 추천 시 예외 발생")
    void toggleLike_userNotFound_throwsException() {
        UUID reviewId = UUID.randomUUID();
        Review review = buildReview(reviewId, "추천할 리뷰입니다.");

        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.of(review));
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.toggleLike(reviewId, userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("본인 리뷰 삭제 성공")
    void deleteReview_success() {
        UUID reviewId = UUID.randomUUID();
        Review review = buildReview(reviewId, "삭제할 리뷰입니다.");
        ReviewImage image = ReviewImage.builder()
                .review(review)
                .imageUrl("https://cdn.example.com/review/image.jpg")
                .displayOrder(0)
                .build();

        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.of(review));
        given(reviewImageRepository.findByReviewIdAndDeletedAtIsNullOrderByDisplayOrderAsc(reviewId))
                .willReturn(List.of(image));

        ReviewDeleteResponse response = reviewService.deleteReview(reviewId, userId);

        assertThat(response.isDeleted()).isTrue();
        assertThat(response.getReviewId()).isEqualTo(reviewId);
        assertThat(review.getDeletedAt()).isNotNull();
        assertThat(image.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 리뷰 삭제 시 예외 발생")
    void deleteReview_reviewNotFound_throwsException() {
        UUID reviewId = UUID.randomUUID();
        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteReview(reviewId, userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    @DisplayName("본인 리뷰가 아니면 삭제할 수 없다")
    void deleteReview_forbidden_throwsException() {
        UUID reviewId = UUID.randomUUID();
        Review review = buildReview(reviewId, "다른 사용자의 리뷰입니다.");

        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).willReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.deleteReview(reviewId, UUID.randomUUID()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_APPLICATION_FORBIDDEN);
    }

    @Test
    @DisplayName("홈 노출 리뷰 목록을 노출 순서대로 조회")
    void listHomeReviews_success() {
        UUID reviewId = UUID.randomUUID();
        Review review = buildReview(reviewId, "홈에 노출할 리뷰입니다.");
        review.updateHomeFeatured(true, 1);
        ReviewImage image = ReviewImage.builder()
                .review(review)
                .imageUrl("https://cdn.example.com/review/home.jpg")
                .displayOrder(0)
                .build();

        given(reviewRepository.findByIsHomeFeaturedTrueAndDeletedAtIsNullOrderByHomeDisplayOrderAscCreatedAtDesc())
                .willReturn(List.of(review));
        given(reviewImageRepository.findByReviewIdInAndDeletedAtIsNullOrderByDisplayOrderAsc(List.of(reviewId)))
                .willReturn(List.of(image));

        List<HomeReviewResponse> response = reviewService.listHomeReviews();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getReviewId()).isEqualTo(reviewId);
        assertThat(response.get(0).getNickname()).isEqualTo(user.getNickname());
        assertThat(response.get(0).getGatheringTitle()).isEqualTo(gathering.getTitle());
        assertThat(response.get(0).getThumbnailImageUrl()).isEqualTo("https://cdn.example.com/review/home.jpg");
        assertThat(response.get(0).getHomeDisplayOrder()).isEqualTo(1);
    }

    private Review buildReview(UUID reviewId, String content) {
        Review review = Review.builder()
                .user(user)
                .application(application)
                .gathering(gathering)
                .reviewType(ReviewType.TEXT)
                .reviewContent(content)
                .build();
        ReflectionTestUtils.setField(review, "id", reviewId);
        return review;
    }
}
