package com.whatsuphouse.backend.domain.application.client.dto;

import com.whatsuphouse.backend.global.common.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class ApplicationRequest {

    @Schema(example = "770e8400-e29b-41d4-a716-446655440002")
    private UUID gatheringId;

    @Schema(example = "김철수", description = "비회원 필수")
    private String name;

    @Schema(example = "01012345678", description = "비회원 필수")
    private String phone;

    @Schema(example = "MALE")
    private Gender gender;

    @Schema(example = "28")
    private Integer age;

    @Schema(example = "chulsoo_kim")
    private String instagramId;

    @Schema(example = "디자이너")
    private String job;

    @Schema(example = "ENFP")
    private String mbti;

    @Schema(example = "재즈를 좋아해서 신청합니다")
    private String intro;

    @Schema(example = "이영희")
    private String referrerName;
}
