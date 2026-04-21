# /implement

구글 드라이브 기획 문서를 읽고, Jira 이슈를 생성하고, 코드를 구현하고, 검토 후 완료 처리까지 자동으로 수행한다.

## 사용법

```
/implement {구현할 기능 설명}
```

예시:
- `/implement 게더링 목록 조회 API`
- `/implement 모임 신청 API`

---

## 실행 순서

### 1단계 — 문서 읽기 (spec-reader)

`.claude/agent/spec-reader.md` 역할을 수행한다.

- Google Drive에서 관련 문서를 검색한다
- 요청 기능과 관련된 섹션만 읽고 요약한다
- Jira 이슈 템플릿(KAN-1) 구조로 정리한다:
  `[설명] [요구사항] [API] [Request] [Response] [DB] [예외]`

### 2단계 — Jira 이슈 생성

- `atlassianUserInfo`로 현재 사용자 accountId를 조회한다
- 1단계 요약 내용으로 Jira 이슈를 생성한다
  - projectKey: `KAN`
  - issueTypeName: `Feature`
  - assignee: 현재 로그인 사용자
  - Start date(`customfield_10015`): 오늘 날짜 (YYYY-MM-DD)
- 생성된 이슈키를 확인한다 (예: `KAN-3`)

### 3단계 — 브랜치 생성

생성된 이슈키와 도메인 코드를 기반으로 브랜치를 만들고 체크아웃한다.

브랜치 컨벤션:
```
{타입}/{이슈키}-{도메인코드}-{간략설명}
```
- 타입: `feature`(신규), `fix`(버그), `refactor`(리팩토링)
- 도메인코드: `auth` `usr` `gth` `app` `loc` `adm`

```bash
git checkout -b feature/KAN-{번호}-{도메인}-{설명}
```

### 4단계 — 코드 구현 (create-api)

`.claude/agent/create-api.md` 역할을 수행한다.

- `.claude/rules/backend/api.md`, `.claude/rules/backend/jpa.md` 규칙을 준수한다
- 기존 유사 도메인 코드 스타일을 참고한다
- 생성 순서: Entity → Repository → DTO → Service → Controller
- 이미 구현된 코드가 있으면 규칙 위반 여부만 확인한다

### 5단계 — 검토 (backend-reviewer)

`.claude/agent/backend-reviewer.md` 역할을 수행한다.

- 아키텍처, DTO/Entity 분리, JPA, API 규칙을 검토한다
- 문제가 있으면 즉시 수정한다
- 수정 완료 후 6단계로 진행한다

### 6단계 — Jira 완료 처리

- Jira 이슈에 검토 결과 코멘트를 추가한다
- 이슈 상태를 "완료"로 전환한다

---

## 참고 agent

- `.claude/agent/spec-reader.md`
- `.claude/agent/create-api.md`
- `.claude/agent/backend-reviewer.md`
