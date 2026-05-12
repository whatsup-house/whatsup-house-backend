package com.whatsuphouse.backend.domain.carousel.admin.dto.request;

import com.whatsuphouse.backend.domain.carousel.enums.SlideType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CarouselSlideCreateRequest {

    @NotNull
    @Schema(example = "GATHERING")
    private SlideType type;

    @NotBlank
    @Size(max = 200)
    @Schema(example = "5월 소풍 모임")
    private String title;

    @Size(max = 500)
    @Schema(example = "함께 봄나들이 떠나요")
    private String content;

    @NotBlank
    @Size(max = 500)
    @Schema(example = "https://cdn.example.com/slide.jpg")
    private String imageUrl;

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID gatheringId;

    @Schema(example = "0")
    private Integer sortOrder;
}
