# API Rules

이 문서는 이 프로젝트에서 API 설계, 컨트롤러 구현, 요청/응답 처리 시 따라야 할 규칙을 정의한다.

---

## 1. 기본 원칙

- API는 클라이언트와의 계약이다.
- 내부 구현보다 외부 인터페이스의 일관성과 안정성을 우선한다.
- 기존 API 응답 구조와 경로 규칙을 임의로 깨지 않는다.

---

## 2. Controller 역할

- Controller는 HTTP 요청/응답 처리만 담당한다.
- 비즈니스 로직은 Service에서 처리한다.
- Controller는 요청 파싱, 인증 주체 전달, 응답 반환에 집중한다.

### 금지

- 비즈니스 로직 작성
- 엔티티 직접 조작
- 복잡한 조건 분기 처리

---

## 3. 요청 처리 규칙

- 요청값은 DTO로 받는다.
- `@RequestBody`, `@PathVariable`, `@RequestParam`의 역할을 명확히 구분한다.
- 엔티티를 요청 객체로 직접 사용하지 않는다.
- 요청 검증은 DTO에서 가능한 범위까지 처리하고, 복잡한 검증은 Service에서 처리한다.
- DTO는 일반 클래스를 사용하고, record는 지양한다.
- Request DTO의 모든 필드에는 `@Schema(example = "...")` 를 반드시 추가한다.

### 입력 검증 규칙

Request DTO의 모든 필드에 적절한 Bean Validation 어노테이션을 반드시 추가한다.

| 상황 | 어노테이션 |
|------|-----------|
| 필수 문자열 | `@NotBlank` |
| 필수 객체/ID/숫자 | `@NotNull` |
| 문자열 길이 제한 | `@Size(min = X, max = Y)` |
| 숫자 범위 제한 | `@Min(X)`, `@Max(Y)` |
| 이메일 형식 | `@Email` |
| 정규식 형식 | `@Pattern(regexp = "...")` |

- Controller의 `@RequestBody` 파라미터에 반드시 `@Valid`를 붙인다.
- 검증 오류는 공통 예외 처리 흐름(`@MethodArgumentNotValidException` 핸들러)이 처리한다.
- Controller에서 직접 검증 오류를 핸들링하지 않는다.

### 예시

```java
// DTO
public class GatheringCreateRequest {
    @Schema(example = "4월 홍대 소셜 게더링")
    @NotBlank
    private String title;

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @NotNull
    private UUID locationId;

    @Schema(example = "10")
    @NotNull
    @Min(2) @Max(20)
    private Integer maxAttendees;
}

// Controller
public ResponseEntity<?> createGathering(@RequestBody @Valid GatheringCreateRequest request) { ... }
```

---

## 4. 응답 처리 규칙

- 모든 API 응답은 `ApiResponse<T>` 형태로 반환한다.
- Controller는 엔티티가 아니라 Response DTO를 반환한다.
- 엔티티를 API 응답으로 직접 노출하지 않는다.
- Response DTO는 `from(entity)` 패턴을 허용한다.

### 예시

```java
return ResponseEntity.ok(ApiResponse.success(responseDto));
````

---

## 5. 예외 처리 규칙

* 예외는 `CustomException(ErrorCode)`를 사용한다.
* Controller에서 불필요한 try-catch를 작성하지 않는다.
* 예외 응답은 공통 예외 처리 흐름을 따른다.
* 직접 `RuntimeException`, `IllegalArgumentException`을 던지지 않는다.
* `ErrorCode`에 적절한 항목이 없으면 새로 추가하고 사용한다.

### 예외 처리 패턴

**조회 실패**
```java
User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
```

**중복 확인**
```java
if (userRepository.existsByEmail(email)) {
    throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
}
```

**상태/권한 검증**
```java
if (gathering.getStatus() != GatheringStatus.OPEN) {
    throw new CustomException(ErrorCode.GATHERING_NOT_RECRUITING);
}
```

모든 비즈니스 예외는 위 패턴처럼 즉시 명시적으로 던진다. 조건을 흘려보내거나 null 반환으로 대응하지 않는다.

---

## 6. API 경로 규칙

* 인증 API: `/api/auth/**` → 공개
* 관리자 API: `/api/admin/**` → ROLE_ADMIN 필요
* 유저 API: `/api/**` → JWT 인증 필요
* 공개 조회 API: `GET /api/gatherings/**` → 인증 없이 접근 가능

### 원칙

* 경로는 리소스 중심으로 작성한다.
* 동사형 경로보다 명사형 리소스 경로를 우선한다.

### 예시

* `/api/gatherings`
* `/api/gatherings/{id}`
* `/api/admin/gatherings/{id}`

### 지양

* `/api/getGathering`
* `/api/createGathering`

---

## 7. Service 연계 규칙

* Service는 기본적으로 `@Transactional` 적용을 전제로 한다.
* 조회 전용 로직은 필요 시 `readOnly = true`를 고려한다.
* Controller는 Service를 호출해 유스케이스 흐름을 위임한다.

---

## 8. Swagger / OpenAPI 규칙

* API를 추가하거나 수정하면 Swagger 문서도 함께 확인한다.
* 요청/응답 구조는 Swagger와 실제 구현이 일치해야 한다.
* Swagger는 API 탐색과 계약 확인에 사용하지만, 자동화 테스트의 대체재로 간주하지 않는다.

### Swagger UI

* `/swagger-ui/index.html`

---

## 9. 코딩 컨벤션

* 응답은 `ApiResponse<T>` 래퍼를 사용한다.
* 예외는 `CustomException(ErrorCode)`를 사용한다.
* DTO는 일반 클래스를 사용한다.

### Lombok 사용

* `@Getter`
* `@Builder`
* `@RequiredArgsConstructor`

### 지양

* 불필요한 `@Setter`
* API DTO에 record 사용
* Controller에서 직접 엔티티 반환

---

## 10. 금지 사항

* Controller에 비즈니스 로직을 넣지 않는다.
* 엔티티를 요청/응답 객체처럼 사용하지 않는다.
* API 응답 구조를 임의로 변경하지 않는다.
* Swagger 문서와 실제 API 구현을 불일치 상태로 두지 않는다.
* 인증/인가 판단을 Controller에서 직접 처리하지 않는다.

---

## 11. 작업 체크리스트

### API 추가 시

* 요청 DTO를 만들었는가?
* 응답 DTO를 만들었는가?
* `ApiResponse<T>`로 감쌌는가?
* 인증/권한 규칙이 맞는가?
* Swagger 확인이 필요한가?

### API 수정 시

* 기존 응답 구조가 깨지지 않는가?
* 공개/인증/관리자 경로 규칙이 맞는가?
* 엔티티가 직접 노출되지 않는가?
* 예외 처리가 공통 규칙을 따르는가?
