package com.whatsuphouse.backend.domain.home.repository;

import com.whatsuphouse.backend.domain.home.entity.HomeReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HomeReviewRepository extends JpaRepository<HomeReview, UUID> {

    List<HomeReview> findByIsActiveTrueOrderByDisplayOrderAscCreatedAtDesc();
}
