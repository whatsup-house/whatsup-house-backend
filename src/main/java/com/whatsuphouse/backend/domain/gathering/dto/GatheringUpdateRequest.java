package com.whatsuphouse.backend.domain.gathering.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
public class GatheringUpdateRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private UUID locationId;

    @NotNull
    private LocalDate eventDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @Positive
    private Integer price;

    @NotNull
    @Positive
    private Integer maxAttendees;

    private String thumbnailUrl;
}
