package com.whatsuphouse.backend.domain.location.dto;

import com.whatsuphouse.backend.domain.location.entity.Location;
import com.whatsuphouse.backend.domain.location.enums.LocationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class LocationCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    private String mapUrl;

    @NotNull
    @Positive
    private Integer maxCapacity;

    private LocationStatus status = LocationStatus.ACTIVE;

    private String memo;

    public Location toEntity() {
        return Location.builder()
                .name(name)
                .address(address)
                .mapUrl(mapUrl)
                .maxCapacity(maxCapacity)
                .status(status != null ? status : LocationStatus.ACTIVE)
                .memo(memo)
                .build();
    }
}
