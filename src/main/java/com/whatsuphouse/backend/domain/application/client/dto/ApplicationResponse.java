package com.whatsuphouse.backend.domain.application.client.dto;

import com.whatsuphouse.backend.domain.application.entity.Application;
import com.whatsuphouse.backend.domain.application.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ApplicationResponse {

    private UUID id;
    private String bookingNumber;
    private UUID gatheringId;
    private ApplicationStatus status;
    private LocalDateTime createdAt;

    public static ApplicationResponse from(Application application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .bookingNumber(application.getBookingNumber())
                .gatheringId(application.getGathering().getId())
                .status(application.getStatus())
                .createdAt(application.getCreatedAt())
                .build();
    }
}
