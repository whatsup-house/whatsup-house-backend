package com.whatsuphouse.backend.domain.location.client.dto;

import com.whatsuphouse.backend.domain.location.entity.Location;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class LocationDetailResponse {

    private UUID id;
    private String name;
    private String address;
    private String mapUrl;

    public static LocationDetailResponse from(Location location) {
        return LocationDetailResponse.builder()
                .id(location.getId())
                .name(location.getName())
                .address(location.getAddress())
                .mapUrl(location.getMapUrl())
                .build();
    }
}
