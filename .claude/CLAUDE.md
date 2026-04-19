# CLAUDE.md

이 문서는 Claude Code가 이 백엔드 프로젝트에서 작업할 때 따라야 할 규칙을 정의한다.

---

## 0. 서비스 소개

와썹하우스(Whats Up House)는 1인 가구 2030 청년을 위한 오프라인 소셜 게더링 플랫폼.
- 대표(관리자)가 게더링을 직접 주최하고 운영
- 유저는 게더링을 탐색하고 신청만 함 (유저 간 개인 매칭 없음)
- 게더링은 항상 소규모 (최대 20명)

## 1. 프로젝트 컨텍스트

이 프로젝트는 Spring Boot 기반 REST API 서버이며,
도메인 중심 구조를 사용해 각 도메인이 독립적으로 비즈니스 로직을 갖도록 설계되어 있다.

게더링 플랫폼 특성상 관리자 주도 운영 구조이며,
유저 간 직접 매칭이 아닌 “게더링 신청” 흐름을 중심으로 동작한다.

---

## 2. 실행 및 테스트 명령어

```bash
./gradlew bootRun
./gradlew build
./gradlew clean build
./gradlew test
./gradlew test --tests "com.whatsuphouse.backend.SomeTest"
````

Swagger:

* `/swagger-ui/index.html`

---

## 3. 아키텍처 구조

프로젝트는 도메인 중심 + 계층 구조를 따른다.

```
domain/{name}/
  controller/
  service/
  repository/
  entity/
  dto/
```

Cross-cutting:

* `global/config`
* `global/auth`
* `global/exception`
* `global/common`

---

## 4. 아키텍처 핵심 규칙

### 역할 분리

* controller: HTTP 요청/응답 처리만 담당
* service: 유스케이스 흐름 및 트랜잭션 관리
* repository: DB 접근
* entity: 도메인 상태 + 행위
* dto: 요청/응답 전용 객체

controller에 비즈니스 로직을 작성하지 않는다.

---

### 엔티티 설계 기준

* 엔티티는 단순 데이터 객체가 아니라 상태와 행위를 함께 가진다.

허용:

* 상태 변경 메서드 (예: changeStatus, updateProfile)
* 자기 상태 기반 검증 로직

금지:

* DTO 변환 책임 (toDto, fromDto 등)
* Repository, Service 등 외부 의존

---

### DTO 규칙

* DTO는 요청/응답 전용 객체로 사용한다.

허용:

* Response DTO → from(entity)

제한:

* Request DTO → 단순한 경우만 toEntity() 허용

원칙:

* 복잡한 생성/조합 로직은 서비스에서 처리한다.

---

### 서비스 설계 기준

* 서비스는 유스케이스 단위 흐름을 담당한다.
* 트랜잭션 경계를 가진다.
* 여러 엔티티를 조합하는 로직을 포함한다.

역할:

* 엔티티 조회
* 비즈니스 흐름 제어
* 엔티티 메서드 호출로 상태 변경
* 저장 및 결과 반환

원칙:

* 상태 변경은 엔티티 메서드를 우선 활용한다.
* 서비스는 단순 중계 계층이 아니다.

---

## 5. API 및 응답 규칙

* 모든 API는 ApiResponse<T> 형태로 응답한다.
* 예외는 CustomException(ErrorCode)를 사용한다.

경로 규칙:

* /api/auth/** → 공개
* /api/** → 인증 필요
* /api/admin/** → 관리자 권한 필요

---

## 6. JPA 및 데이터 처리 규칙

* ddl-auto: validate 사용 (스키마 자동 변경 없음)
* 엔티티 변경 시 DB 스키마 영향 여부를 반드시 확인한다.

Soft Delete:

* deletedAt = null → 활성 데이터
* 조회 시 soft delete 데이터 포함 여부를 고려한다.

주의:

* Lazy Loading 및 연관관계 사용 시 성능 영향 고려
* 엔티티 변경 시 기존 쿼리 영향 확인

---

## 7. 테스트 정책

* 테스트는 무조건 작성하지 않는다.
* 핵심 비즈니스 로직, 변경 위험이 높은 부분 위주로 작성한다.

원칙:

* 의미 없는 커버리지 테스트 작성 금지
* Swagger는 API 확인 도구이며 테스트를 대체하지 않는다.

---

## 8. 작업 시 주의 사항

* 엔티티를 API 응답으로 직접 반환하지 않는다 (DTO 사용)
* 기존 패키지 구조를 임의로 변경하지 않는다
* 대규모 리팩토링은 요청이 있을 때만 수행한다
* 변경은 최소 단위로 수행한다
* 기존 코드 스타일을 우선적으로 따른다

---

## 9. 도메인 정보

* User
* Gathering
* Application
* Location
---

## 추가 코딩 컨벤션

### Service

- Service 계층에는 기본적으로 @Transactional을 적용한다.
- 조회 전용 메서드는 필요 시 readOnly = true를 고려한다.

---

### DTO

- DTO는 일반 클래스를 사용한다.
- record 사용은 지양한다.

---

### Lombok

- Lombok 사용을 기본으로 한다.

사용 권장:

- @Getter
- @Builder
- @RequiredArgsConstructor

지양:

- 불필요한 @Setter 남용
