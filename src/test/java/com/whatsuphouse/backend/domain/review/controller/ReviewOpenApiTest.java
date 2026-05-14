package com.whatsuphouse.backend.domain.review.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReviewOpenApiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Swagger 문서에 리뷰 작성 API 스펙이 노출된다")
    void reviewCreateApi_isExposedInOpenApiDocs() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api-docs", String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("\"/api/reviews\"");
        assertThat(response.getBody()).contains("\"post\"");
        assertThat(response.getBody()).contains("applicationId");
        assertThat(response.getBody()).contains("reviewContent");
        assertThat(response.getBody()).contains("imageTempPaths");
        assertThat(response.getBody()).doesNotContain("rating");
    }

    @Test
    @DisplayName("Swagger 문서에 전체 리뷰 목록 조회 API 스펙이 노출된다")
    void reviewListApi_isExposedInOpenApiDocs() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api-docs", String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("\"/api/reviews\"");
        assertThat(response.getBody()).contains("\"get\"");
        assertThat(response.getBody()).contains("sort");
        assertThat(response.getBody()).contains("page");
        assertThat(response.getBody()).contains("size");
    }

    @Test
    @DisplayName("Swagger 문서에 게더링별 리뷰 목록 조회 API 스펙이 노출된다")
    void gatheringReviewListApi_isExposedInOpenApiDocs() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api-docs", String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("\"/api/gatherings/{gatheringId}/reviews\"");
        assertThat(response.getBody()).contains("\"get\"");
        assertThat(response.getBody()).contains("gatheringId");
        assertThat(response.getBody()).contains("sort");
        assertThat(response.getBody()).contains("page");
        assertThat(response.getBody()).contains("size");
    }
}
