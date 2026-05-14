# SonarQube 코드 리뷰 보고서

> 분석일: 2026-05-14  
> 프로젝트: `whatsup-house-backend`  
> Quality Gate: **PASSED**

---

## 전체 지표 요약

| 지표 | 값 | 등급 |
|------|-----|------|
| 코드 라인 수 | 2,156 | - |
| 커버리지 | 63.2% | - |
| 버그 | ~~1~~ 0 | ~~B~~ A |
| 취약점 | 0 | A |
| 코드 스멜 | 29 | A |
| 중복 코드 | 0.0% | - |
| 유지보수성 | - | A |

---

## CRITICAL — 즉시 수정 필요

### 1. ~~`@Transactional` 메서드를 `this`로 직접 호출 (버그)~~ ✅ 수정 완료
- **파일**: `ApplicationService.java`
- **규칙**: `java:S6809`
- **수정 내용**: `apply()` 로직을 `private applyInternal()`로 추출. `apply()`와 `applyAsGuest()` 각각 독립적으로 프록시를 통해 `@Transactional` 적용 후 `applyInternal()` 호출.
- **커밋**: `fix: ApplicationService @Transactional self-call 프록시 우회 버그 수정 (KAN-S6809)`

### 2. ~~문자열 리터럴 중복 — `SupabaseStorageService.java`~~ ✅ 수정 완료
- **파일**: `SupabaseStorageService.java`
- **규칙**: `java:S1192`
- `AUTHORIZATION = "Authorization"`, `BEARER_PREFIX = "Bearer "` 상수 추가 후 3개 호출부 모두 교체
- **커밋**: `refactor: SupabaseStorageService, AuthController 중복 문자열 상수화`

### 3. ~~문자열 리터럴 중복 — `AuthController.java`~~ ✅ 수정 완료
- **파일**: `AuthController.java`
- **규칙**: `java:S1192`
- `ACCESS_TOKEN = "accessToken"`, `REFRESH_TOKEN = "refreshToken"` 상수 추가 후 전체 호출부 교체
- **커밋**: 위와 동일

---

## MAJOR — 우선 처리 권장

### 4. ~~사용되지 않는 로컬 변수 할당~~ ✅ 수정 완료
- **파일**: `ApplicationRepositoryTest.java`
  `app1` 변수 할당 제거 → `saveApplication()` 반환값 무시로 수정 (158, 187번 라인)
- **파일**: `AdminApplicationServiceTest.java`
  미사용 `UUID id = applicationId;` 라인 제거 (347번 라인)
- **커밋**: `fix: 테스트 코드 미사용 로컬 변수 할당 제거 (S1854, S1481)`

---

## MINOR — 코드 품질 개선 ✅ 전체 수정 완료

### 테스트 Assertion 개선 (S5838) ✅
- `.isEqualTo(0)` → `.isZero()` 적용 파일: `AdminCarouselServiceTest`, `CarouselSlideRepositoryTest`, `AuthServiceTest`, `AdminUserServiceTest`, `UserServiceTest`, `AdminUserRepositoryTest`, `AdminGatheringServiceTest`
- `CarouselSlideRepositoryTest.java` → `assertThat(result.get()).isEqualTo(5)` → `assertThat(result).contains(5)` (Optional 직접 검증)
- `AdminUserRepositoryTest.java` → `doesNotContain` 전에 `assertThat(result.getContent()).isNotEmpty()` 추가 (S5841)

### 불필요한 import 제거 (S1128) ✅
- `AdminUserRepositoryTest.java` — 같은 패키지 `UserApplicationStatsProjection` import 제거
- `AdminApplicationServiceTest.java` — 미사용 `ArgumentMatchers.any`, `ArgumentMatchers.eq` import 제거

### 미사용 로컬 변수 제거 (S1481) ✅
- `ApplicationRepositoryTest.java` — 미사용 변수 `app1` 제거 (MAJOR에서 수정 완료)
- `AdminApplicationServiceTest.java` — 미사용 변수 `id` 제거 (MAJOR에서 수정 완료)

### 기타 ✅
- `SupabaseStorageService.java` — `PATH_SEPARATOR = "/"` 상수 추가, `tempPath` 조합에 적용 (S1075)
- `AdminApplicationServiceTest.java` — 빈 statement `;;` → `;` 수정 (S1116)
- `AdminApplicationServiceTest.java` — Mockito `eq()` 제거, 값 직접 전달로 변경 (S6068)
- `AdminCarouselService.java` — `request.getIsActive()` → `Boolean.TRUE.equals(request.getIsActive())` (S5411)

**커밋**: `fix: SonarQube MINOR 이슈 전체 수정`

---

## 우선순위별 Action Plan

| 우선순위 | 항목 | 담당 |
|---------|------|------|
| ~~P0~~ | ~~`ApplicationService.java:93` — 트랜잭션 우회 버그 수정~~ | ✅ 완료 |
| ~~P1~~ | ~~`SupabaseStorageService`, `AuthController` 문자열 상수화~~ | ✅ 완료 |
| ~~P2~~ | ~~테스트 코드 미사용 변수 및 빈 statement 정리~~ | ✅ 완료 (변수 제거) |
| ~~P3~~ | ~~Assertion 스타일 통일 (`isZero`, `contains`)~~ | ✅ 완료 |
| ~~P3~~ | ~~불필요한 import 제거~~ | ✅ 완료 |
