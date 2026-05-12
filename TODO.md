# TODO

## KAN-69: 홈 Curated Section 게더링 랭킹 API (FR-HOME-15)

### 개요
- `GET /api/home/curated` — 큐레이션 게더링 목록 조회 (비인증)
- 1차 릴리즈: 관리자 수동 지정, 향후 자동 랭킹 전환 가능한 구조
- DB 방안 B: `gatherings` 테이블에 `is_curated`, `curated_rank` 컬럼 추가
- 관리자 UX: 활성화 토글 + 순서 일괄 변경 (carousel과 동일 패턴)

### 작업 목록

#### Entity / Repository
- [ ] `Gathering.java` — `isCurated`, `curatedRank` 필드 추가 + `updateCuration()` 메서드
- [ ] `GatheringRepository.java` — `findByIsCuratedTrueAndDeletedAtIsNullOrderByCuratedRankAsc()` 추가

#### 클라이언트 API
- [ ] `CuratedGatheringResponse.java` 신규 — `id, title, thumbnailUrl, eventDate, locationName, price, status, curatedRank`
- [ ] `GatheringService.java` — `listCuratedGatherings()` 추가
- [ ] `GatheringController.java` — `GET /api/home/curated` 추가
- [ ] `SecurityConfig.java` — `PERMIT_GET`에 `/api/home/curated` 추가

#### 관리자 API
- [ ] `GatheringCurationRequest.java` 신규 — `isCurated (boolean)`
- [ ] `GatheringCurationOrderRequest.java` 신규 — `gatheringIds (List<UUID>)`
- [ ] `AdminGatheringService.java` — `toggleCuration()`, `reorderCurated()` 추가
- [ ] `AdminGatheringController.java` — `PATCH /{id}/curation`, `PUT /curated/order` 추가

### 검증
- `GET /api/home/curated` 비인증 호출 → 빈 배열 200
- 관리자 큐레이션 지정 후 재조회 → curatedRank 순 정렬 확인
- `./gradlew test` 전체 통과

---

## KAN-TBD: Supabase Storage 이미지 업로드 API

### 개요
- 현재 캐러셀 슬라이드(`imageUrl`)와 게더링(`thumbnailUrl`)은 이미지 URL을 직접 입력하는 구조
- 관리자가 이미지를 업로드하면 Supabase Storage에 저장하고 URL을 반환하는 API 필요
- 반환된 URL을 캐러셀/게더링 등록·수정 시 `imageUrl`, `thumbnailUrl` 필드에 사용

### 적용 대상
- `carousel` — 슬라이드 이미지 (`imageUrl`)
- `gathering` — 게더링 썸네일 (`thumbnailUrl`)
- 향후 유저 아바타 등으로 확장 가능

### 작업 목록

#### 인프라 / 설정
- [ ] `build.gradle` — Supabase Storage Java 클라이언트 또는 S3 호환 SDK 의존성 추가
- [ ] `application.yml` — `supabase.url`, `supabase.key`, `supabase.bucket` 설정 추가
- [ ] `.env` — Supabase 환경변수 추가 (`SUPABASE_URL`, `SUPABASE_KEY`, `SUPABASE_BUCKET`)

#### 공통 인프라
- [ ] `global/storage/SupabaseStorageClient.java` — Supabase Storage REST API 연동 클라이언트
- [ ] `global/storage/StorageService.java` — 파일 업로드 후 public URL 반환

#### 관리자 API
- [ ] `ImageUploadResponse.java` — `{ "url": "https://..." }` 응답 DTO
- [ ] `AdminImageController.java` — `POST /api/admin/images` (Multipart 파일 수신 → Storage 업로드 → URL 반환)
- [ ] `SecurityConfig.java` — `/api/admin/images` ADMIN 권한 추가

### 검증
- 이미지 업로드 → Supabase Storage 버킷에 파일 저장 확인
- 반환된 URL로 이미지 직접 접근 가능 확인
- 캐러셀 슬라이드 생성 시 반환된 URL을 `imageUrl`로 사용해 정상 저장 확인
- 허용 확장자 외 파일 업로드 시 400 응답 확인
