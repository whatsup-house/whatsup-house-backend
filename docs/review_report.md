# 백엔드 코드 리뷰 보고서

> 리뷰 일자: 2026-05-11
> 대상: 전체 도메인 (auth, user, location, carousel, gathering, application) + global 패키지

---

## auth 도메인

### 문제점

- **[심각도: 높음]** `domain/auth/controller/AuthController.java:92-101` — `extractCookie()`가 null을 반환할 때 `AuthService.refresh()`에 그대로 전달된다. "쿠키 없음"과 "유효하지 않은 토큰"을 동일하게 처리하여 Controller에서 조건 분기가 암묵적으로 내포된다.

- **[심각도: 중간]** `domain/auth/service/AuthService.java` — 클래스 레벨 `@Transactional`로 인해 `logout()`(Redis 삭제만)과 `refresh()`가 불필요하게 DB 트랜잭션을 점유한다.

- **[심각도: 중간]** `domain/auth/dto/response/LoginResponse.java` — `accessToken`, `refreshToken`이 응답 바디에 포함된다. HttpOnly 쿠키로 발급하는 설계와 충돌하며, 보안 효과가 반감된다.

- **[심각도: 중간]** `domain/auth/controller/AuthController.java:73-79` — `buildCookie()`에 `maxAge`가 지정되지 않아 브라우저 세션 쿠키로 동작한다. Redis refreshToken 만료 시간과 불일치 발생.

- **[심각도: 낮음]** `domain/auth/dto/request/RegisterRequest.java` — `@Schema(description = "...")`가 없고 `example`만 존재한다.

### 개선 제안

- `AuthController.refresh()`에서 쿠키 추출 결과가 null이면 즉시 `CustomException(ErrorCode.INVALID_REFRESH_TOKEN)`을 던진다.
- `AuthService`의 클래스 레벨 `@Transactional`을 제거하고 `register()`, `login()`에만 적용한다. `refresh()`는 `readOnly = true`로 분리.
- `LoginResponse`에서 토큰 필드를 제거하고 쿠키로만 전달한다.
- `buildCookie()`에 JWT 만료 시간 기반 `maxAge`를 설정한다.

---

## user 도메인

### 문제점

- **[심각도: 높음]** `domain/user/entity/User.java:73-84` — `updateProfile()`이 null 값을 그대로 필드에 덮어쓴다. `phone`, `name`, `gender`, `age` 등이 null로 전달되면 DB에 null 저장.

- **[심각도: 높음]** `domain/user/repository/UserRepository.java:11` — `findByEmail()`에 `AndDeletedAtIsNull` 조건이 없다. `existsByEmail`, `existsByNickname`도 탈퇴 유저를 포함해 체크한다.

- **[심각도: 중간]** `domain/user/service/UserService.java:52` — `findActiveUser()`가 `public`으로 선언되어 있다. 내부 헬퍼 메서드는 `private`으로 제한해야 한다.

- **[심각도: 중간]** `domain/user/dto/response/ProfileResponse.java` — `gender` 필드가 누락되어 있다.

- **[심각도: 중간]** `domain/user/repository/UserRepositoryCustomImpl.java:64-68` — count 쿼리 `fetchOne()` 결과에 null 체크가 없어 NullPointerException 위험.

- **[심각도: 낮음]** `domain/user/dto/request/ProfileUpdateRequest.java` — `instagramId`, `job`, `intro` 필드에 `@Size` 등 길이 검증이 없다.

- **[심각도: 낮음]** `domain/user/admin/dto/response/UserAdminListResponse.java:39` — 정적 팩토리 메서드명이 `of`다. `from()`으로 통일 필요.

### 개선 제안

- `User.updateProfile()`에 null 체크 후 조건부 업데이트를 적용한다.
- `findByEmail()` → `findByEmailAndDeletedAtIsNull()`, `existsByEmail()` / `existsByNickname()`에도 `AndDeletedAtIsNull` 추가.
- `findActiveUser()`를 `private`으로 변경한다.
- `ProfileResponse`에 `gender` 필드를 추가한다.
- count 쿼리를 `Optional.ofNullable(fetchOne()).orElse(0L)`로 감싼다.
- `ProfileUpdateRequest` 필드에 컬럼 정의와 일치하는 `@Size` 검증을 추가한다.
- `UserAdminListResponse.of()` → `from()`으로 변경.

---

## location 도메인

### 문제점

- **[심각도: 높음]** `domain/location/common/dto/response/LocationDetailResponse.java:21-30` — `status`, `maxCapacity` 필드가 응답에서 누락되어 있다. 수정 후 클라이언트가 변경 결과를 알 수 없다.

- **[심각도: 높음]** `domain/location/common/dto/response/LocationResponse.java:18-26` — 목록 조회 응답에 `status` 필드가 없어 클라이언트가 `EXPIRED` 장소를 필터링할 수단이 없다.

- **[심각도: 중간]** `domain/location/repository/LocationRepository.java:12` — `findByDeletedAtIsNull()` → `findAllByDeletedAtIsNull()`로 네이밍 규칙 위반.

- **[심각도: 중간]** `domain/location/admin/dto/request/LocationCreateRequest.java:31,42-43` — `status` 기본값이 이미 선언되어 있는데 `toEntity()` 내부에서 중복 null 가드.

- **[심각도: 낮음]** `domain/location/client/controller/LocationController.java:19` — Security 설정에서 `/api/locations` 공개 허용 여부가 불분명하다.

### 개선 제안

- `LocationDetailResponse`와 `LocationResponse` 모두에 `status` 필드 추가, `LocationDetailResponse`에는 `maxCapacity`도 추가.
- `findByDeletedAtIsNull()` → `findAllByDeletedAtIsNull()`로 변경하고 호출부도 수정.
- `LocationCreateRequest.toEntity()`의 중복 null 가드 제거.

---

## carousel 도메인

### 문제점

- **[심각도: 높음]** `domain/carousel/admin/service/AdminCarouselService.java:121-125` — `reorderSlides()`에서 `sortOrder` 하나만 변경하면 되는데 `update()`로 모든 필드를 재할당한다. 의도하지 않은 더티 체킹 유발.

- **[심각도: 높음]** `domain/carousel/admin/controller/AdminCarouselController.java:63` — `PATCH /{slideId}`(toggleActive)와 `PUT /{slideId}`(updateSlide)가 같은 경로 패턴을 공유한다. `PATCH /{slideId}/active`로 분리 필요.

- **[심각도: 높음]** `domain/carousel/admin/controller/AdminCarouselController.java:74` — `PUT /order`가 `PUT /{slideId}` 패턴과 충돌 가능성 존재. Spring 내부 동작에 의존하는 취약한 구조.

- **[심각도: 중간]** `domain/carousel/admin/service/AdminCarouselService.java:41-68` — `createSlide()`에 검증, gathering 조회, sortOrder 계산, 엔티티 생성 로직이 혼재한다. 타입별 분기 로직이 엔티티 팩토리 메서드로 이동 가능.

- **[심각도: 중간]** `domain/carousel/admin/dto/response/AdminCarouselSlideResponse.java`, `domain/carousel/common/dto/response/CarouselSlideResponse.java` — 두 `from()` 메서드의 `dateLabel`/`gatheringId` 추출 로직이 완전히 중복.

- **[심각도: 낮음]** `domain/carousel/entity/CarouselSlide.java:77-79` — `delete()` 래퍼 메서드가 `super.delete()`만 위임하는 불필요한 코드.

### 개선 제안

- `CarouselSlide` 엔티티에 `updateSortOrder(int order)` 메서드를 추가하고 `reorderSlides()`에서 사용.
- `toggleActive` 경로를 `PATCH /{slideId}/active`로 분리.
- `reorderSlides` 경로를 `PUT /reorder`로 변경.
- `GATHERING` 타입 시 `content` null 처리 분기를 엔티티 팩토리 메서드로 이동.
- `CarouselSlide.delete()` 래퍼 메서드 제거.

---

## gathering 도메인

### 문제점

- **[심각도: 높음]** `domain/gathering/repository/GatheringRepository.java:27` — `findByIdAndDeletedAtIsNull`에 `@EntityGraph`가 없어 `location` 지연 로딩 발생. `GatheringDetailResponse.from()`에서 `location` 필드를 접근하므로 트랜잭션 경계 외부 호출 시 LazyInitializationException 위험.

- **[심각도: 중간]** `domain/gathering/client/controller/GatheringController.java:30` — 목록 조회 메서드명이 `getGatherings`. SKILL.md 규칙상 `listGatherings`이어야 한다. Service도 동일.

- **[심각도: 중간]** `domain/gathering/common/dto/response/GatheringResponse.java`, `GatheringDetailResponse.java` — 모든 필드에 `@Schema(description = "...", example = "...")` 누락.

- **[심각도: 낮음]** `domain/gathering/admin/controller/AdminGatheringController.java:68` — `changeStatus`가 `ApiResult.success("...", null)`로 변경된 상태 정보를 응답에 포함하지 않는다.

- **[심각도: 낮음]** `domain/gathering/admin/service/AdminGatheringService.java:84-95` — `createGathering`에서 빌더 조립 로직이 Service에 있다. `updateGathering`은 엔티티 `update()` 메서드를 사용해 일관성이 없다.

### 개선 제안

- `findByIdAndDeletedAtIsNull`에 `@EntityGraph(attributePaths = "location")` 추가.
- `getGatherings` → `listGatherings`으로 메서드명 수정 (Controller, Service 모두).
- `GatheringResponse`, `GatheringDetailResponse` 전체 필드에 `@Schema` 추가.
- `changeStatus` 응답에 변경된 상태값 포함.

---

## application 도메인

### 문제점

- **[심각도: 높음]** `domain/application/admin/service/AdminApplicationService.java:40` — `deleteApplication`에서 `findById()`를 사용해 soft delete 조회 일관성 규칙을 위반한다.

- **[심각도: 높음]** `domain/application/repository/ApplicationRepositoryCustomImpl.java:34-38` — `findApplications` 쿼리에 `gathering`, `user` fetch join이 없어 N+1 발생. `AdminApplicationResponse.from()`에서 두 연관관계 필드를 접근한다.

- **[심각도: 높음]** `domain/application/client/service/ApplicationService.java:92-94` — `applyAsGuest`가 `apply(..., null)`을 호출해 회원/비회원 분기가 메서드 내부에 혼재. 단일 책임 원칙 위반.

- **[심각도: 중간]** `domain/application/repository/ApplicationRepository.java:39` — `findByUserIdAndDeletedAtIsNull`에 `@EntityGraph`가 없어 `gathering` N+1 발생. `ApplicationListResponse.from()`에서 gathering 필드를 접근한다.

- **[심각도: 중간]** `domain/application/client/dto/request/ApplicationRequest.java` — `name`, `phone` 필드에 Bean Validation이 없고 서비스 레이어에서 직접 검증한다. SKILL.md 규칙 위반.

- **[심각도: 중간]** `domain/application/client/controller/ApplicationController.java:25` — 클래스 레벨 `@RequestMapping`이 없고 경로가 메서드마다 개별 선언되어 분산.

- **[심각도: 중간]** `domain/application/client/dto/response/ApplicationListResponse.java:27` — `GatheringInfo.eventDate` 타입이 `String`. `Gathering.eventDate`는 `LocalDate`이므로 타입 일관성 위반.

- **[심각도: 낮음]** `domain/application/admin/dto/request/AdminApplicationStatusRequest.java:14` — `@Schema description`에 `CANCELLED`를 상태로 명시하나 실제 switch 문에서 처리되지 않아 Swagger 문서와 동작 불일치.

- **[심각도: 낮음]** Response DTO 전체 필드에 `@Schema` 어노테이션 누락.

### 개선 제안

- `AdminApplicationService.deleteApplication`의 `findById()` 호출을 `findByIdAndDeletedAtIsNull`로 교체하거나, 멱등 처리용 전용 Repository 메서드를 명시적으로 분리한다.
- `ApplicationRepositoryCustomImpl.findApplications`에 `join(application.gathering).fetchJoin()`, `join(application.user).fetchJoin()` 추가.
- `ApplicationRepository.findByUserIdAndDeletedAtIsNull`에 `@EntityGraph(attributePaths = "gathering")` 추가.
- 회원/비회원 신청 로직을 private 메서드로 분리한다.
- `ApplicationRequest` 필수 필드에 `@NotBlank` 등 Bean Validation 추가.
- `GatheringInfo.eventDate` 타입을 `LocalDate`로 교체.
- `AdminApplicationStatusRequest`의 `@Schema description`에서 `CANCELLED` 제거.
- Response DTO 전체에 `@Schema` 어노테이션 추가.

---

---

## global 패키지

### auth (JWT/인증)

#### 문제점

- **[심각도: 높음]** `global/auth/JwtAuthFilter.java:29` — 토큰 검증 실패와 토큰 없음을 구분하지 않고 조용히 통과시킨다. 만료 토큰도 필터를 통과해 Security 인가 단계에서만 차단된다.

- **[심각도: 높음]** `global/auth/JwtTokenProvider.java:54-60` — `validateToken`이 `JwtException`을 포괄 처리해 만료(`ExpiredJwtException`)와 위변조(`SignatureException`)를 구분하지 않는다. `TOKEN_EXPIRED` 에러코드가 정의되어 있으나 실제로 사용되지 않는 사문 코드.

- **[심각도: 중간]** `global/auth/JwtAuthFilter.java:39-53` — Authorization 헤더와 쿠키 두 경로에서 모두 토큰을 수락한다. CSRF 비활성화 상태에서 쿠키 기반 인증을 허용하면 CSRF 공격 벡터가 생긴다.

- **[심각도: 낮음]** `global/auth/JwtTokenProvider.java:63-70` — `getUserIdFromToken`과 `getUserPrincipal`이 클레임 파싱을 각각 독립 수행한다. 전자는 후자의 부분집합이므로 중복.

#### 개선 제안

- `validateToken` 내부에서 `ExpiredJwtException`을 별도 catch로 분리하고, 만료 토큰 감지 시 `JwtAuthFilter`에서 `401 TOKEN_EXPIRED` 응답을 즉시 반환한다.
- 쿠키 인증을 제거하거나 `SameSite=Strict` 정책을 명시적으로 적용한다.
- `getUserIdFromToken`을 제거하고 호출부를 `getUserPrincipal(...).getUserId()`로 교체한다.

---

### config (설정)

#### 문제점

- **[심각도: 높음]** `global/config/SecurityConfig.java:61` — `anyRequest().permitAll()`로 경로 매칭 누락 시 무조건 공개 허용된다. 최소 권한 원칙에 따라 `denyAll()` 또는 `authenticated()`가 안전하다.

- **[심각도: 높음]** `global/config/SecurityConfig.java:51-53` — `authenticationEntryPoint`가 Servlet 기본 에러 형식으로 응답한다. 다른 API 오류는 `ApiResult<Void>` 형태인데 인증 실패만 포맷이 불일치한다.

- **[심각도: 중간]** `global/config/SecurityConfig.java:71` — CORS `allowedOrigins`에 `http://localhost:3000`이 하드코딩되어 있다. 프로덕션 설정에 로컬 주소가 포함된 보안 정책 혼입.

- **[심각도: 중간]** `global/config/SecurityConfig.java:57-58` — 일부 `PERMIT_ALL` 경로가 배열이 아닌 인라인 `requestMatchers`로 선언되어 관리 포인트가 분산된다.

- **[심각도: 낮음]** `global/config/QuerydslConfig.java:13` — `@PersistenceContext`로 `EntityManager` 필드 주입. 생성자 주입 방식으로 통일 필요.

- **[심각도: 낮음]** `global/config/SecurityConfig.java:33` — `PERMIT_ALL`에 `/api-docs/**` 포함. SpringDoc 기본 경로는 `/v3/api-docs/**`이므로 실제 동작 경로 확인 필요.

#### 개선 제안

- `anyRequest().permitAll()` → `anyRequest().denyAll()`로 교체한다.
- `authenticationEntryPoint`에서 `ObjectMapper`로 `ApiResult.fail(...)` JSON을 직접 write한다.
- `allowedOrigins`를 `application.yml` 프로파일별 설정으로 이동하고 `@Value`로 주입한다.
- `/api-docs/**` → `/v3/api-docs/**`로 수정 확인.

---

### exception (예외 처리)

#### 문제점

- **[심각도: 중간]** `global/exception/GlobalExceptionHandler.java:43-51` — `MethodArgumentNotValidException` 처리 시 `findFirst()`로 첫 번째 오류 하나만 반환해 복수 필드 오류를 클라이언트가 한 번에 인지할 수 없다.

- **[심각도: 낮음]** `global/exception/ErrorCode.java:20` — `TOKEN_EXPIRED`가 정의되어 있으나 실제 사용되지 않는 사문 코드 (JWT auth 문제와 연동).

- **[심각도: 낮음]** `global/exception/ErrorCode.java:11,26` — `// User` 주석이 두 번 등장한다.

#### 개선 제안

- `JwtTokenProvider.validateToken`에서 `ExpiredJwtException`을 분리해 `TOKEN_EXPIRED`를 실제로 사용한다.
- `ErrorCode.java`의 `// User` 중복 주석을 정리한다.

---

### common (공통 클래스)

#### 문제점

- **[심각도: 중간]** `global/common/ApiResult.java` — 클래스명이 `ApiResult`인데 SKILL.md 규칙에는 `ApiResponse<T>`로 명시되어 있다. 문서와 구현체 명칭 불일치.

- **[심각도: 낮음]** `global/common/BaseEntity.java:29-31` — `delete()`가 `LocalDateTime.now()`를 직접 사용해 테스트 시 시간 제어가 불가능하다.

- **[심각도: 낮음]** `global/common/enums/Gender.java`, `Mbti.java` — user 도메인 전용이라면 `domain/user/` 하위로 이동이 더 적합하다.

#### 개선 제안

- `ApiResult` → `ApiResponse`로 클래스명을 변경하거나 SKILL.md를 `ApiResult`로 통일한다.
- `BaseEntity.delete()`의 `LocalDateTime.now()`를 Clock 주입 방식으로 교체해 테스트 가능성을 확보한다.

---

## 심각도별 요약

| 심각도 | 건수 | 주요 영역 |
|--------|------|----------|
| 높음   | 16건 | user, application, auth, global/config |
| 중간   | 20건 | carousel, application, gathering, global |
| 낮음   | 13건 | @Schema 누락, 네이밍, 중복 코드 |

### 우선 처리 권장 항목

1. **`anyRequest().permitAll()`** — `SecurityConfig` (보안 정책 허점, 즉시 수정)
2. **N+1 쿼리** — `application` 2건, `gathering` 1건 (성능 직결)
3. **soft delete 조회 일관성** — `user`, `application` (데이터 무결성)
4. **null 덮어쓰기** — `user.updateProfile()` (데이터 손실 위험)
5. **토큰 만료 미구분** — `JwtTokenProvider.validateToken` (TOKEN_EXPIRED 사문 코드)
6. **인증 실패 응답 포맷 불일치** — `authenticationEntryPoint` (클라이언트 혼란)
7. **토큰 응답 바디 노출** — `auth.LoginResponse` (보안)
8. **carousel 경로 충돌** — `AdminCarouselController` (런타임 라우팅 오류)
