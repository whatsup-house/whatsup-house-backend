package com.whatsuphouse.backend.domain.auth.dto;

import com.whatsuphouse.backend.global.common.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class RegisterRequest {

    @NotBlank
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "비밀번호는 영문과 숫자를 포함해야 합니다.")
    private String password;

    @NotBlank
    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하여야 합니다.")
    private String nickname;

    @Pattern(regexp = "^\\d{11}$", message = "전화번호는 11자리 숫자여야 합니다.")
    private String phone;

    @NotBlank
    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
    private String name;

    @NotNull(message = "성별을 입력해주세요.")
    private Gender gender;

    @NotNull(message = "나이를 입력해주세요.")
    @Min(value = 1, message = "나이는 1 이상이어야 합니다.")
    private Integer age;
}
