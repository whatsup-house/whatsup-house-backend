package com.whatsuphouse.backend.domain.location.dto;

import com.whatsuphouse.backend.domain.location.enums.LocationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class LocationUpdateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    private String mapUrl;

    @NotNull
    @Positive
    private Integer maxCapacity;

    @NotNull
    private LocationStatus status;

    private String memo;
}
