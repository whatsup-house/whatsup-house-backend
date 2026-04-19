package com.whatsuphouse.backend.domain.user.dto;

import com.whatsuphouse.backend.global.common.enums.Gender;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ProfileUpdateRequest {

    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하여야 합니다.")
    private String nickname;

    @Pattern(regexp = "^\\d{11}$", message = "전화번호는 11자리 숫자여야 합니다.")
    private String phone;

    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
    private String name;

    private Gender gender;

    @Min(value = 1, message = "나이는 1 이상이어야 합니다.")
    private Integer age;
}
