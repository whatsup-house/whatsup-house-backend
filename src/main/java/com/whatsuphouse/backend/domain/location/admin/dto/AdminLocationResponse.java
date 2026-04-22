package com.whatsuphouse.backend.domain.location.admin.dto;

import com.whatsuphouse.backend.domain.location.entity.Location;
import com.whatsuphouse.backend.domain.location.enums.LocationStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AdminLocationResponse {

    private UUID id;
    private String name;
    private String address;
    private String mapUrl;
    private int maxCapacity;
    private LocationStatus status;

    public static AdminLocationResponse from(Location location) {
        return AdminLocationResponse.builder()
                .id(location.getId())
                .name(location.getName())
                .address(location.getAddress())
                .mapUrl(location.getMapUrl())
                .maxCapacity(location.getMaxCapacity())
                .status(location.getStatus())
                .build();
    }
}
