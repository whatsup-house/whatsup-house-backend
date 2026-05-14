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

### 2. 문자열 리터럴 중복 — `SupabaseStorageService.java`
- **파일**: `SupabaseStorageService.java:49`
- **규칙**: `java:S1192`
- `"Authorization"` 문자열이 3회 반복됨 → `private static final String AUTHORIZATION = "Authorization";`으로 상수화
- `"Bearer "` 문자열이 3회 반복됨 → `private static final String BEARER_PREFIX = "Bearer ";`으로 상수화

### 3. 문자열 리터럴 중복 — `AuthController.java`
- **파일**: `AuthController.java:47-48`
- **규칙**: `java:S1192`
- `"accessToken"` 문자열이 3회 반복됨 → 상수화
- `"refreshToken"` 문자열이 4회 반복됨 → 상수화

---

## MAJOR — 우선 처리 권장

### 4. 사용되지 않는 로컬 변수 할당
- **파일**: `ApplicationRepositoryTest.java:158, 187`  
  로컬 변수 `app1`에 값을 할당하지만 이후 사용되지 않음 → 변수 제거 또는 코드 의도 재확인
- **파일**: `AdminApplicationServiceTest.java:347`  
  로컬 변수 `id`에 값을 할당하지만 이후 사용되지 않음 → 변수 제거

---

## MINOR — 코드 품질 개선

### 테스트 Assertion 개선 (S5838)
아래 파일에서 `.isEqualTo(0)` 대신 `.isZero()` 사용 권장.

| 파일 | 라인 |
|------|------|
| `AdminCarouselServiceTest.java` | 240, 409 |
| `CarouselSlideRepositoryTest.java` | 84 |
| `AuthServiceTest.java` | 137 |
| `AdminUserServiceTest.java` | 64, 78, 83 |
| `UserServiceTest.java` | 65, 90 |
| `AdminUserRepositoryTest.java` | 115 |
| `AdminGatheringServiceTest.java` | 108 |

- `CarouselSlideRepositoryTest.java:119` → `.contains()` 사용 권장
- `AdminUserRepositoryTest.java:147` → List 비어있는지 먼저 확인 후 assertion (S5841)

### 불필요한 import 제거 (S1128)
- `AdminUserRepositoryTest.java:8` — 같은 패키지 클래스 import 불필요
- `AdminApplicationServiceTest.java:34` — 미사용 `ArgumentMatchers.any` import

### 미사용 로컬 변수 제거 (S1481)
- `ApplicationRepositoryTest.java:158, 187` — 미사용 변수 `app1`
- `AdminApplicationServiceTest.java:347` — 미사용 변수 `id`

### 기타
- `SupabaseStorageService.java:44` — 하드코딩된 path delimiter `/` → `File.separator` 또는 상수 사용 (S1075)
- `AdminApplicationServiceTest.java:353` — 빈 statement(세미콜론 단독 줄) 제거 (S1116)
- `AdminApplicationServiceTest.java:261` — Mockito `eq()` 불필요하게 사용 → 값 직접 전달 (S6068)
- `AdminCarouselService.java:116` — `Boolean` 박싱 타입 대신 원시 `boolean` 사용 (S5411)

---

## 우선순위별 Action Plan

| 우선순위 | 항목 | 담당 |
|---------|------|------|
| ~~P0~~ | ~~`ApplicationService.java:93` — 트랜잭션 우회 버그 수정~~ | ✅ 완료 |
| P1 | `SupabaseStorageService`, `AuthController` 문자열 상수화 | - |
| P2 | 테스트 코드 미사용 변수 및 빈 statement 정리 | - |
| P3 | Assertion 스타일 통일 (`isZero`, `contains`) | - |
| P3 | 불필요한 import 제거 | - |
