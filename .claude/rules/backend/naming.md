# Naming 컨벤션

이 문서는 이 프로젝트에서 클래스명, 필드명, 메서드명에 대한 네이밍 규칙을 정의한다.

---

## 1. 클래스명 규칙

| 레이어 | 형식 | 예시 |
|--------|------|------|
| Entity | 도메인명 (단수) | `User`, `Gathering`, `Application`, `Location` |
| Controller (유저) | `{도메인}Controller` | `GatheringController`, `UserController` |
| Controller (관리자) | `Admin{도메인}Controller` | `AdminGatheringController`, `AdminApplicationController` |
| Service (유저) | `{도메인}Service` | `UserService`, `GatheringService` |
| Service (관리자) | `Admin{도메인}Service` | `AdminGatheringService`, `AdminApplicationService` |
| Repository | `{도메인}Repository` | `UserRepository`, `GatheringRepository` |
| Request DTO | `{동작}{도메인}Request` | `GatheringCreateRequest`, `ProfileUpdateRequest`, `LoginRequest` |
| Response DTO | `{도메인}{범위}Response` | `GatheringResponse`, `GatheringDetailResponse`, `ProfileResponse` |
| Enum | 대문자 단수형 | `GatheringStatus`, `Gender`, `ApplicationStatus` |

### 주의

- `Dto` 접미사는 사용하지 않는다 (`GatheringDto` → `GatheringResponse` 사용)
- 관리자/유저 분리가 필요한 경우 패키지로 분리 (`admin/`, `client/`)

---

## 2. 필드명 규칙

- 모든 Java 필드: **camelCase**
- DB 컬럼: **snake_case** (JPA 자동 매핑 또는 `@Column(name = "...")` 명시)
- boolean 플래그: `is` 접두어 사용 (`isAdmin`, `isAvailable`)
- 날짜/시간: `{의미}At` (과거 시점), `{의미}Date`, `{의미}Time` 형태
  - 예: `createdAt`, `deletedAt`, `eventDate`, `startTime`
- 연관 엔티티 참조: 객체명 그대로 (`location`, `gathering`, `user`)
- 외래키만 저장할 때: `{도메인}Id` (`locationId`, `gatheringId`)

---

## 3. Enum 값 규칙

- **UPPER_SNAKE_CASE** 사용
- 의미가 명확한 단어 선택

### 예시

```java
public enum GatheringStatus { OPEN, CLOSED, COMPLETED, CANCELLED }
public enum ApplicationStatus { PENDING, CONFIRMED, CANCELLED, ATTENDED }
public enum Gender { MALE, FEMALE }
```

---

## 4. Service 메서드명 규칙

| 동작 | 형식 | 예시 |
|------|------|------|
| 단건 조회 | `get{대상}` | `getProfile`, `getGathering` |
| 목록 조회 | `list{복수대상}` | `listGatherings`, `listApplications` |
| 내부 조회 헬퍼 | `find{대상}` | `findActiveUser`, `findGathering` |
| 생성 | `create{대상}` | `createGathering`, `createApplication` |
| 등록 (회원) | `register{대상}` | `registerUser` |
| 수정 | `update{대상}` | `updateProfile`, `updateGathering` |
| 상태 변경 | `change{대상}Status` | `changeGatheringStatus` |
| 삭제 | `delete{대상}` | `deleteGathering` |
| 취소 | `cancel{대상}` | `cancelApplication` |
| 확인 (boolean) | `is{조건}` | `isEmailAvailable`, `isNicknameAvailable` |

---

## 5. Repository 메서드명 규칙

- Soft delete 조회 시 반드시 `AndDeletedAtIsNull` 조건 포함
- 단건 조회: `findBy{조건}AndDeletedAtIsNull`
- 전체 조회: `findAllBy{조건}AndDeletedAtIsNull`
- 존재 확인: `existsBy{필드}`, `existsBy{필드}AndDeletedAtIsNull`

### 예시

```java
Optional<User> findByIdAndDeletedAtIsNull(UUID id);
Optional<User> findByEmailAndDeletedAtIsNull(String email);
List<Gathering> findAllByDeletedAtIsNull();
boolean existsByEmail(String email);
boolean existsByNickname(String nickname);
```

---

## 6. 패키지 구조 규칙

```
domain/{name}/
  controller/         # 유저용 API
    admin/            # 관리자용 API (복잡할 경우 분리)
    client/           # 유저용 API (복잡할 경우 분리)
  service/
  repository/
  entity/
  dto/
    request/
    response/
```

---

## 7. 금지 사항

- 약어 남발 (`usrSvc`, `gthrCtrl`) — 의미가 드러나는 전체 이름 사용
- 숫자로 끝나는 클래스명 (`Gathering2`, `UserNew`)
- `Manager`, `Helper`, `Utils` 접미어를 Service 대신 사용
- DTO에 `toDto()`, `fromDto()`, `toEntity()` (단순 생성 외)를 엔티티 안에 두기
