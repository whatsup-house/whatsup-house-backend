package com.whatsuphouse.backend.domain.review.service;

import com.whatsuphouse.backend.domain.application.entity.Application;
import com.whatsuphouse.backend.domain.application.enums.ApplicationStatus;
import com.whatsuphouse.backend.domain.application.repository.ApplicationRepository;
import com.whatsuphouse.backend.domain.gathering.entity.Gathering;
import com.whatsuphouse.backend.domain.review.client.dto.request.ReviewCreateRequest;
import com.whatsuphouse.backend.domain.review.client.dto.response.ReviewResponse;
import com.whatsuphouse.backend.domain.review.client.service.ReviewService;
import com.whatsuphouse.backend.domain.review.entity.ReviewImage;
import com.whatsuphouse.backend.domain.review.enums.ReviewType;
import com.whatsuphouse.backend.domain.review.repository.ReviewImageRepository;
import com.whatsuphouse.backend.domain.review.repository.ReviewRepository;
import com.whatsuphouse.backend.domain.user.entity.User;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewImageRepository reviewImageRepository;

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
}
