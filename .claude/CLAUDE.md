# CLAUDE.md

이 문서는 Claude Code가 이 백엔드 프로젝트에서 작업할 때 따라야 할 핵심 원칙을 정의한다.
세부 규칙은 `.claude/rules/` 하위 문서를 참고한다.

---

## 0. 서비스 소개

와썹하우스(Whats Up House)는 1인 가구 2030 청년을 위한 오프라인 소셜 게더링 플랫폼.
- 대표(관리자)가 게더링을 직접 주최하고 운영
- 유저는 게더링을 탐색하고 신청만 함 (유저 간 개인 매칭 없음)
- 게더링은 항상 소규모 (최대 20명)

---

## 1. 프로젝트 컨텍스트

Spring Boot 기반 REST API 서버. 도메인 중심 구조로 각 도메인이 독립적으로 비즈니스 로직을 가진다.

---

## 2. 실행 및 테스트 명령어

```bash
./gradlew bootRun
./gradlew build
./gradlew clean build
./gradlew test
./gradlew test --tests "com.whatsuphouse.backend.SomeTest"
```

Swagger: `/swagger-ui/index.html`

---

## 3. 아키텍처 구조

```
domain/{name}/
  admin/
    controller/   ← 관리자 전용 API
    service/      ← 관리자 전용 비즈니스 로직
    dto/          ← 관리자 전용 요청/응답 DTO (Admin 접두사)
  client/
    controller/   ← 유저 전용 API
    service/      ← 유저 전용 비즈니스 로직
    dto/          ← 유저 전용 요청/응답 DTO
  entity/         ← 도메인 엔티티 (공유)
  repository/     ← DB 접근 (공유)
  enums/          ← 열거형 (공유, 도메인에 따라)
```

- admin/client 기능이 없는 도메인(auth 등)은 하위 분리 없이 단일 구조 유지
- admin DTO는 `Admin` 접두사를 붙인다 (AdminLocationResponse 등)
- entity, repository, enums는 admin/client 공유 자원이므로 domain 루트에 둔다

Cross-cutting:
- `global/config`
- `global/auth`
- `global/exception`
- `global/common`

---

## 4. 핵심 역할 분리 원칙

- **controller**: HTTP 요청/응답 처리만 — 비즈니스 로직 금지
- **service**: 유스케이스 흐름 + 트랜잭션 관리
- **repository**: DB 접근만
- **entity**: 도메인 상태 + 행위 (DTO 변환 책임 금지)
- **dto**: 요청/응답 전용

> 세부 규칙 → `.claude/rules/backend/api.md`, `.claude/rules/backend/jpa.md`

---

## 5. 도메인 정보

- User
- Gathering
- Application
- Location

---

## 6. 테스트 정책

- 의미 없는 커버리지 테스트 작성 금지
- 핵심 비즈니스 로직, 변경 위험이 높은 부분 위주로만 작성

---

## 7. 작업 시 주의 사항

- 엔티티를 API 응답으로 직접 반환하지 않는다 (DTO 사용)
- 기존 패키지 구조를 임의로 변경하지 않는다
- 대규모 리팩토링은 요청이 있을 때만 수행한다
- 변경은 최소 단위로 수행한다
- 기존 코드 스타일을 우선적으로 따른다

---

## 8. 코딩 컨벤션

- Lombok 기본 사용: `@Getter`, `@Builder`, `@RequiredArgsConstructor`
- 불필요한 `@Setter` 남용 지양
- DTO는 일반 클래스 사용 (record 지양)
- Service 기본 `@Transactional`, 조회 전용은 `readOnly = true` 고려
