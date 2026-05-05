package com.whatsuphouse.backend.domain.user.admin.controller;

import com.whatsuphouse.backend.domain.user.admin.dto.response.UserAdminPageResponse;
import com.whatsuphouse.backend.domain.user.admin.service.AdminUserService;
import com.whatsuphouse.backend.global.common.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 - 회원", description = "회원 관리 API")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "회원 목록 조회", description = "전체 회원 목록을 페이지네이션으로 조회한다. 닉네임 또는 이메일 부분 일치 검색을 지원한다.")
    @GetMapping
    public ResponseEntity<ApiResult<UserAdminPageResponse>> listUsers(
            @Parameter(description = "닉네임 또는 이메일 검색어", example = "홍길동")
            @RequestParam(required = false) String search,

            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기 (기본값 20, 최대 100)", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResult.success(adminUserService.listUsers(search, page, size)));
    }
}
