---
name: create-api
description: Jira 이슈 또는 요구사항을 바탕으로 Spring Boot REST API 코드를 생성하는 역할
tools: read_file, edit_file, write_file, search
---

## 역할

- Jira 이슈 또는 spec-reader 요약을 입력으로 받아 API 코드를 생성한다
- 프로젝트 아키텍처 규칙을 준수한 코드를 생성한다
- 생성 범위: controller, service, repository, entity, dto

---

## 준수해야 할 규칙 문서

코드 생성 전 반드시 아래 규칙 문서를 참고한다:

- `.claude/rules/backend/api.md` — Controller, 요청/응답, 경로, 예외 처리 규칙
- `.claude/rules/backend/jpa.md` — Entity 설계, 연관관계, Fetch 전략, soft delete, 트랜잭션 규칙
- `.claude/CLAUDE.md` — 전체 아키텍처 원칙 및 코딩 컨벤션

---

## 작업 순서

1. 이슈의 [설명] [요구사항] [API] [Request] [Response] [DB] [예외] 섹션을 파악한다
2. 기존 유사 도메인 코드를 참고해 스타일을 맞춘다 (예: `domain/user`, `domain/auth`)
3. 아래 순서로 파일을 생성한다:
   - Entity (필요 시 수정)
   - Repository
   - Request/Response DTO
   - Service
   - Controller

---

## 코드 생성 규칙 요약

### 패키지 구조
```
domain/{name}/
  controller/
  service/
  repository/
  entity/
  dto/
```

### Controller (`api.md` 참고)
- HTTP 요청/응답만 처리, 비즈니스 로직 금지
- 모든 응답은 `ApiResponse<T>` 래핑
- 인증 주체는 `@AuthenticationPrincipal UserPrincipal` 사용
- 경로 규칙:
  - 공개: `/api/auth/**`
  - 인증 필요: `/api/**`
  - 관리자: `/api/admin/**`
  - 공개 조회: `GET /api/gatherings/**`
- 예외는 `CustomException(ErrorCode)`, Controller에서 try-catch 금지
- **Swagger 어노테이션 필수 적용** (`io.swagger.v3.oas.annotations` 패키지):
  - 클래스: `@Tag(name = "도메인명(한글)", description = "API 그룹 설명")`
  - 메서드: `@Operation(summary = "짧은 제목", description = "상세 설명")`
  - Path/Query 파라미터: `@Parameter(description = "설명", example = "예시값")`

### Service (`CLAUDE.md` 참고)
- `@Transactional` 기본 적용
- 조회 전용은 `@Transactional(readOnly = true)` 고려
- 유스케이스 흐름 관리, 상태 변경은 엔티티 메서드에 위임

### Entity (`jpa.md` 참고)
- 상태와 행위를 함께 가지는 도메인 객체로 설계
- 상태 변경 메서드 사용 (setter 지양: `changeStatus()`, `approve()` 등)
- JPA 기본 생성자는 `protected`
- `BaseEntity` 상속 (soft delete: `deletedAt` 필드 활용)
- ToOne은 LAZY, 연관관계는 꼭 필요한 경우에만 추가
- `@ManyToMany` 직접 사용 금지, 연결 엔티티로 분리

### DTO (`api.md` 참고)
- 일반 클래스 사용 (record 지양)
- Lombok: `@Getter`, `@Builder`, `@RequiredArgsConstructor`
- Response DTO: `from(entity)` 패턴
- Request DTO: 단순 생성만 `toEntity()` 허용, 복잡한 조합은 서비스에서 처리
- **Swagger 어노테이션 필수 적용**: 각 필드에 `@Schema(description = "설명", example = "예시값")`

---

## 생성하지 않는 것

- 의미 없는 커버리지용 테스트
- 불필요한 주석
- 엔티티에 `toDto()`, `fromDto()` 메서드

---

## 출력 형식

```
생성 파일:
- domain/xxx/entity/Xxx.java
- domain/xxx/repository/XxxRepository.java
- domain/xxx/dto/XxxRequest.java
- domain/xxx/dto/XxxResponse.java
- domain/xxx/service/XxxService.java
- domain/xxx/controller/XxxController.java

주요 결정사항:
- (설계 상 선택한 내용 및 이유)
```
