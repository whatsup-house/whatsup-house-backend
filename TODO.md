## KAN-69: 홈 Curated Section 게더링 랭킹 API (FR-HOME-15)

### 개요
- `GET /api/home/curated` — 큐레이션 게더링 목록 조회 (비인증)
- 관리자 수동 지정, 수동 순서 변경
- `gatherings` 테이블에 `is_curated`, `curated_rank` 컬럼 직접 추가
- 관리자 UX: 활성화 토글 + 순서 일괄 변경 (carousel과 동일 패턴)

### 작업 목록

#### DB 마이그레이션
- [ ] `V4__add_curation_fields_to_gatherings.sql` — `is_curated`, `curated_rank` 컬럼 추가

#### Entity / Repository
- [ ] `Gathering.java` — `isCurated`, `curatedRank` 필드 추가 + `updateCuration()`, `updateCuratedRank()` 메서드
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