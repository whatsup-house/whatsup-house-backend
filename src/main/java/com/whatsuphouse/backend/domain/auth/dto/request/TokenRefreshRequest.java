package com.whatsuphouse.backend.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TokenRefreshRequest {

    @Schema(example = "eyJhbGciOiJIUzI1NiJ9...")
    @NotBlank
    private String refreshToken;
}
