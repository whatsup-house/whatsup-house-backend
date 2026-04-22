package com.whatsuphouse.backend.domain.gathering.admin.dto;

import com.whatsuphouse.backend.domain.gathering.enums.GatheringStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class AdminGatheringStatusRequest {

    @Schema(example = "CLOSED")
    @NotNull
    private GatheringStatus status;
}
