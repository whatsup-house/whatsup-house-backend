package com.whatsuphouse.backend.domain.location.admin.dto;

import com.whatsuphouse.backend.domain.location.entity.Location;
import com.whatsuphouse.backend.domain.location.enums.LocationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AdminLocationDetailResponse {

    private UUID id;
    private String name;
    private String address;
    private String mapUrl;
    private int maxCapacity;
    private LocationStatus status;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminLocationDetailResponse from(Location location) {
        return AdminLocationDetailResponse.builder()
                .id(location.getId())
                .name(location.getName())
                .address(location.getAddress())
                .mapUrl(location.getMapUrl())
                .maxCapacity(location.getMaxCapacity())
                .status(location.getStatus())
                .memo(location.getMemo())
                .createdAt(location.getCreatedAt())
                .updatedAt(location.getUpdatedAt())
                .build();
    }
}
