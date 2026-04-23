
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

### 0단계 — 사전 연결 체크

작업 시작 전, 아래 세 가지 연결 상태를 먼저 확인한다:
- Google Drive MCP: `search_files`로 테스트 조회
- Jira MCP: `atlassianUserInfo`로 현재 사용자 조회
- gh CLI: `gh auth status`로 GitHub 인증 확인

⚠️ 하나라도 실패하면 **즉시 작업을 중단**하고 사용자에게 아래 형식으로 보고한다:

> "❌ [Google Drive MCP / Jira MCP / gh CLI] 연결이 확인되지 않아 작업을 중단합니다. 연결 상태를 확인 후 다시 실행해주세요."
> gh CLI 미인증 시: "`gh auth login`으로 인증 후 다시 실행해주세요."

**이후 단계(1~7단계)는 절대 진행하지 않는다.** 세 가지 모두 정상 응답이 확인된 후에만 1단계로 넘어간다.

### 1단계 — 문서 읽기 (spec-reader)

`.claude/agent/spec-reader.md` 역할을 수행한다.

- `search_files`로 관련 문서를 검색한다
- **검색 결과 스니펫에 필요한 섹션이 충분히 포함되어 있으면 `read_file_content`를 호출하지 않는다**
- 스니펫이 잘려 내용이 불충분할 때만 `read_file_content`로 전문을 읽는다
- 읽은 내용을 Jira 이슈 템플릿(KAN-1) 구조로 정리한다:
  `[설명] [요구사항] [API] [Request] [Response] [DB] [예외]`

### 2단계 — Jira 이슈 생성

`.claude/rules/jira.md`의 이슈 생성 규칙, ADF 템플릿을 따른다.

- `atlassianUserInfo`로 현재 사용자 accountId를 조회한다
- 1단계 요약 내용으로 Jira 이슈를 생성한다
- 생성된 이슈키를 확인한다 (예: `KAN-3`)

### 3단계 — 브랜치 생성

`.claude/rules/git.md`의 브랜치 컨벤션을 따른다.

develop 브랜치를 최신화한 후 분기한다:

```bash
git checkout develop
git pull origin develop
git checkout -b {브랜치명}
```

### 4단계 — 코드 구현 (create-api)

`.claude/agent/create-api.md` 역할을 수행한다.

- 구현 전, 동일/인접 도메인의 기존 코드를 읽어 스타일과 패턴을 파악한 후 그대로 따른다
  - **Controller, Service, Repository, DTO 각 1개 파일씩만 읽는다** — 전체 도메인을 읽지 않는다
  - 파일이 길면 `limit` 파라미터로 상단 60줄만 읽는다 (클래스 구조·어노테이션 파악에 충분)
- `.claude/rules/backend/api.md`, `.claude/rules/backend/jpa.md` 규칙을 준수한다
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

### 7단계 — 커밋 & 푸시 & PR 생성 (pr-creator)

`.claude/agent/pr-creator.md` 역할을 수행한다.

- 4단계에서 생성/수정한 파일을 명시적으로 `git add`한다 (`git add .` / `git add -A` 금지)
- 커밋 메시지 컨벤션을 준수해 커밋한다 (브랜치 prefix 기준으로 타입 결정)
- 브랜치를 origin에 push한다
- `.github/PULL_REQUEST_TEMPLATE`을 기반으로 PR을 생성한다
  - PR 개요: Controller 파일의 매핑 어노테이션을 읽어 API 목록 자동 작성
  - Jira 링크: `[{issueKey}](https://whatsuphouse.atlassian.net/browse/{issueKey})` 형식

---

## 참고 agent

- `.claude/agent/spec-reader.md`
- `.claude/agent/create-api.md`
- `.claude/agent/backend-reviewer.md`
- `.claude/agent/pr-creator.md`
