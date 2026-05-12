package com.whatsuphouse.backend.domain.application.admin.dto.response;

import com.whatsuphouse.backend.domain.application.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AdminApplicationStatusResponse {

    private UUID applicationId;
    private ApplicationStatus status;
    private Integer mileageRewarded;
    private Integer userMileageAfter;

    public static AdminApplicationStatusResponse of(UUID applicationId, ApplicationStatus status,
                                                    Integer mileageRewarded, Integer userMileageAfter) {
        return AdminApplicationStatusResponse.builder()
                .applicationId(applicationId)
                .status(status)
                .mileageRewarded(mileageRewarded)
                .userMileageAfter(userMileageAfter)
                .build();
    }
}
