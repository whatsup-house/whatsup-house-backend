package com.whatsuphouse.backend.domain.user.client.controller;

import com.whatsuphouse.backend.domain.user.client.dto.ProfileResponse;
import com.whatsuphouse.backend.domain.user.client.dto.ProfileUpdateRequest;
import com.whatsuphouse.backend.domain.user.client.service.UserService;
import com.whatsuphouse.backend.global.auth.UserPrincipal;
import com.whatsuphouse.backend.global.common.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원", description = "프로필 조회 및 수정 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 프로필 조회")
    @GetMapping("/me")
    public ResponseEntity<ApiResult<ProfileResponse>> getProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResult.success(userService.getProfile(principal.getUserId())));
    }

    @Operation(summary = "내 프로필 수정")
    @PutMapping("/me")
    public ResponseEntity<ApiResult<ProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(ApiResult.success("프로필이 수정되었습니다.", userService.updateProfile(principal.getUserId(), request)));
    }

    @Operation(summary = "닉네임 중복 확인", description = "true: 사용 가능, false: 이미 사용 중")
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResult<Boolean>> checkNickname(
            @Parameter(description = "확인할 닉네임", example = "홍길동닉네임")
            @RequestParam String nickname) {
        return ResponseEntity.ok(ApiResult.success(userService.checkNicknameAvailable(nickname)));
    }

    @Operation(summary = "이메일 중복 확인", description = "true: 사용 가능, false: 이미 사용 중")
    @GetMapping("/check-email")
    public ResponseEntity<ApiResult<Boolean>> checkEmail(
            @Parameter(description = "확인할 이메일", example = "user@example.com")
            @RequestParam String email) {
        return ResponseEntity.ok(ApiResult.success(userService.checkEmailAvailable(email)));
    }
}
