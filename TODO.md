# TODO

## 캐러셀 슬라이드 구현 (KAN-68, KAN-89)

> 기획 확정일: 2026-05-10
> 관련 명세: API명세서 v2.4, 요구사항명세서 v2.4

---

### 1. 명세서 업데이트 (구현 전 선행)

- [ ] 미반영 대기 항목 일괄 반영 (API명세서 v2.5)
  - `date_label` 컬럼/필드 제거 (gathering.event_date에서 파생)
  - `subtitle` → `content` 필드명 반영
  - `CarouselSlideCreateRequest`에서 `dateLabel` 필드 제거
  - 에러코드 `DATE_LABEL_REQUIRED` 삭제
  - sort_order 정책 명시 (중복 허용, 2차 정렬 created_at ASC)

---

### 2. DB 마이그레이션

- [ ] `V3__create_carousel_slides.sql` 작성

```sql
CREATE TABLE carousel_slides (
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    type         VARCHAR(20)  NOT NULL,
    title        VARCHAR(200) NOT NULL,
    content      VARCHAR(500),
    image_url    VARCHAR(500) NOT NULL,
    gathering_id UUID         REFERENCES gatherings(id),
    sort_order   INTEGER      NOT NULL DEFAULT 0,
    is_active    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMP
);
```

---

### 3. 도메인 구현 (`domain/carousel`)

- [ ] `SlideType` enum (CALENDAR, GATHERING, STORY)
- [ ] `CarouselSlide` Entity
  - BaseEntity 상속 (created_at, updated_at, deleted_at)
  - gathering FK (`@ManyToOne`, nullable)
- [ ] `CarouselSlideRepository`
  - 활성 슬라이드 조회: `deleted_at IS NULL AND is_active = true ORDER BY sort_order ASC, created_at ASC`
  - 전체 목록 조회 (관리자용): `deleted_at IS NULL ORDER BY sort_order ASC, created_at ASC`

---

### 4. 퍼블릭 API (KAN-68 / FR-HOME-19)

- [ ] `CarouselSlideResponse` DTO
  - 필드: id, type, title, content, imageUrl, gatheringId, dateLabel(파생), sortOrder
  - dateLabel: type=GATHERING이면 gathering.eventDate 포맷팅, 나머지 null
- [ ] `CarouselService` — 활성 슬라이드 목록 조회
- [ ] `CarouselController` — `GET /api/home/carousel`
- [ ] Spring Security `permitAll` 설정 확인

---

### 5. 관리자 API (KAN-89 / FR-ADM-15~17)

- [ ] `CarouselSlideCreateRequest` DTO
  - 필드: type, title, content, imageUrl, gatheringId, sortOrder
  - 타입별 유효성: GATHERING → gatheringId 필수 / STORY → content 필수
- [ ] `AdminCarouselSlideResponse` DTO
- [ ] `CarouselSlideOrderRequest` DTO (slideIds: UUID[])
- [ ] `AdminCarouselService`
  - 슬라이드 등록 (sort_order 미입력 시 MAX+1 자동 배정)
  - 슬라이드 수정
  - 슬라이드 삭제 (soft delete)
  - 활성/비활성 변경
  - 순서 일괄 변경
- [ ] `AdminCarouselController`
  - `GET    /api/admin/carousel`
  - `POST   /api/admin/carousel`
  - `PUT    /api/admin/carousel/{slideId}`
  - `DELETE /api/admin/carousel/{slideId}`
  - `PATCH  /api/admin/carousel/{slideId}` — isActive 변경
  - `PUT    /api/admin/carousel/order` — 순서 일괄 변경

---

### 6. 예외 처리

- [ ] `SLIDE_NOT_FOUND` — 존재하지 않는 slideId 요청 시
- [ ] `GATHERING_ID_REQUIRED` — type=GATHERING인데 gatheringId 누락
- [ ] `SUBTITLE_REQUIRED` — type=STORY인데 content 누락
- [ ] GATHERING 타입 저장 시 content(subtitle) null 강제, 비GATHERING 타입 저장 시 gatheringId null 강제

---

### 7. 추후 개선 (backlog)

- [ ] 관리자 페이지 드래그 앤 드롭 순서 변경 (현재는 숫자 직접 입력)
- [ ] sort_order 정규화 배치 (장기 운영 시 순서 번호 재정렬)
