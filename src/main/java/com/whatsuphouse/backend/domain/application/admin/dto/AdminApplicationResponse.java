package com.whatsuphouse.backend.domain.application.admin.dto;

import com.whatsuphouse.backend.domain.application.entity.Application;
import com.whatsuphouse.backend.domain.application.enums.ApplicationStatus;
import com.whatsuphouse.backend.global.common.enums.Gender;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AdminApplicationResponse {

    private UUID id;
    private String bookingNumber;
    private String name;
    private String phone;
    private Gender gender;
    private Integer age;
    private String instagramId;
    private String intro;
    private String referrerName;
    private ApplicationStatus status;
    private UUID gatheringId;
    private UUID userId;
    private LocalDateTime createdAt;

    public static AdminApplicationResponse from(Application application) {
        return AdminApplicationResponse.builder()
                .id(application.getId())
                .bookingNumber(application.getBookingNumber())
                .name(application.getName())
                .phone(application.getPhone())
                .gender(application.getGender())
                .age(application.getAge())
                .instagramId(application.getInstagramId())
                .intro(application.getIntro())
                .referrerName(application.getReferrerName())
                .status(application.getStatus())
                .gatheringId(application.getGathering().getId())
                .userId(application.getUser() != null ? application.getUser().getId() : null)
                .createdAt(application.getCreatedAt())
                .build();
    }
}
