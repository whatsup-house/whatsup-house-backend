# Git 컨벤션

이 문서는 이 프로젝트에서 커밋 메시지, 브랜치 명, 푸시 방식에 대한 규칙을 정의한다.

---

## 1. 커밋 메시지 형식

```
{prefix}: {한국어 설명} ({이슈키})
```

### Prefix 정의

| prefix | 용도 |
|--------|------|
| `feat` | 새 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 기능 변화 없는 구조 개선 |
| `chore` | 설정, 빌드, 도구, 의존성 변경 |
| `docs` | 문서만 변경 |
| `test` | 테스트 추가/수정 |

### 이슈키 규칙

- Jira 이슈가 있으면 메시지 끝에 `(KAN-XX)` 포함
- 이슈 없는 경우(전역 설정, 문서 작업 등)는 생략 가능

### 예시

```
feat: 이메일/닉네임 중복 확인 API 구현 (KAN-43)
fix: 게더링 상태 변경 시 NULL 참조 버그 수정 (KAN-50)
refactor: 도메인 구조 정리 및 관리자 API 경로 분리
chore: Swagger 설정 업데이트
docs: API 규칙 문서 보강
```

---

## 2. 브랜치 규칙

### 형식

```
feature/{이슈키}-{짧은-설명}
fix/{이슈키}-{짧은-설명}
```

### 예시

```
feature/KAN-43-usr-check-duplicate
feature/KAN-7-gth-gathering-api
fix/KAN-50-gathering-status-npe
```

### 도메인 약어 (설명 부분에 사용)

| 도메인 | 약어 |
|--------|------|
| user | usr |
| gathering | gth |
| application | app |
| location | loc |
| auth | auth |

---

## 3. 금지 사항

- `WIP`, `update`, `수정`, `작업중` 같은 의미 없는 커밋 메시지
- 여러 도메인의 변경을 한 커밋에 묶기 (관련 없는 변경 혼재 금지)
- 커밋 메시지에 영어/한국어 혼용 (한국어로 통일)
- prefix 없이 메시지만 작성

---

## 4. 작업 흐름

1. Jira 이슈 확인 → 이슈키 메모
2. `feature/{이슈키}-{설명}` 브랜치 생성
3. 작업 단위별로 커밋 (한 커밋 = 한 논리적 변경)
4. PR 생성 (PR template 사용)
5. 리뷰 후 main 또는 develop 병합
