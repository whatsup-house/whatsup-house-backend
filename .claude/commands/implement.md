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

작업 시작 전, 아래 두 MCP 연결 상태를 먼저 확인한다:
- Google Drive MCP: `search_files`로 테스트 조회
- Jira MCP: `GET /rest/api/3/myself` 조회

⚠️ 하나라도 실패하면 즉시 사용자에게 보고하고 중단한다. 연결이 확인된 후에만 다음 단계로 진행한다.

### 1단계 — 문서 읽기 (spec-reader)

`.claude/agent/spec-reader.md` 역할을 수행한다.

- `search_files`로 관련 문서를 검색한다
- **검색 결과 스니펫에 필요한 섹션이 충분히 포함되어 있으면 `read_file_content`를 호출하지 않는다**
- 스니펫이 잘려 내용이 불충분할 때만 `read_file_content`로 전문을 읽는다
- 읽은 내용을 Jira 이슈 템플릿(KAN-1) 구조로 정리한다:
  `[설명] [요구사항] [API] [Request] [Response] [DB] [예외]`

### 2단계 — Jira 이슈 생성

- `atlassianUserInfo`로 현재 사용자 accountId를 조회한다
- 1단계 요약 내용으로 Jira 이슈를 생성한다
  - projectKey: `KAN`
  - issueTypeName: `Feature`
  - assignee: 현재 로그인 사용자
  - Start date(`customfield_10015`): 오늘 날짜 (YYYY-MM-DD)
  - **`contentFormat: adf`** 를 반드시 사용한다 — markdown 포맷은 한글 인코딩이 깨지므로 사용하지 않는다
- 생성된 이슈키를 확인한다 (예: `KAN-3`)

#### Jira 이슈 description ADF 템플릿

`description` 필드에 아래 ADF 구조를 사용한다. 각 섹션의 `text` 값만 채워 넣으면 된다.

```json
{
  "version": 1,
  "type": "doc",
  "content": [
    {
      "type": "heading", "attrs": { "level": 2 },
      "content": [{ "type": "text", "text": "설명" }]
    },
    {
      "type": "paragraph",
      "content": [{ "type": "text", "text": "{한 줄 설명}" }]
    },
    {
      "type": "heading", "attrs": { "level": 2 },
      "content": [{ "type": "text", "text": "요구사항" }]
    },
    {
      "type": "bulletList",
      "content": [
        { "type": "listItem", "content": [{ "type": "paragraph", "content": [{ "type": "text", "text": "{요구사항 항목}" }] }] }
      ]
    },
    {
      "type": "heading", "attrs": { "level": 2 },
      "content": [{ "type": "text", "text": "API" }]
    },
    {
      "type": "paragraph",
      "content": [{ "type": "text", "text": "{METHOD /api/path}", "marks": [{ "type": "code" }] }]
    },
    {
      "type": "heading", "attrs": { "level": 2 },
      "content": [{ "type": "text", "text": "Request" }]
    },
    {
      "type": "paragraph",
      "content": [{ "type": "text", "text": "{요청 파라미터 설명}" }]
    },
    {
      "type": "heading", "attrs": { "level": 2 },
      "content": [{ "type": "text", "text": "Response" }]
    },
    {
      "type": "codeBlock", "attrs": { "language": "json" },
      "content": [{ "type": "text", "text": "{응답 JSON 예시}" }]
    },
    {
      "type": "heading", "attrs": { "level": 2 },
      "content": [{ "type": "text", "text": "DB" }]
    },
    {
      "type": "paragraph",
      "content": [{ "type": "text", "text": "{관련 테이블 및 조건}" }]
    },
    {
      "type": "heading", "attrs": { "level": 2 },
      "content": [{ "type": "text", "text": "예외" }]
    },
    {
      "type": "table",
      "attrs": { "isNumberColumnEnabled": false, "layout": "default" },
      "content": [
        {
          "type": "tableRow",
          "content": [
            { "type": "tableHeader", "attrs": {}, "content": [{ "type": "paragraph", "content": [{ "type": "text", "text": "코드" }] }] },
            { "type": "tableHeader", "attrs": {}, "content": [{ "type": "paragraph", "content": [{ "type": "text", "text": "설명" }] }] }
          ]
        },
        {
          "type": "tableRow",
          "content": [
            { "type": "tableCell", "attrs": {}, "content": [{ "type": "paragraph", "content": [{ "type": "text", "text": "{HTTP 상태코드}" }] }] },
            { "type": "tableCell", "attrs": {}, "content": [{ "type": "paragraph", "content": [{ "type": "text", "text": "{에러 설명}" }] }] }
          ]
        }
      ]
    }
  ]
}
```

### 3단계 — 브랜치 생성

develop 브랜치를 최신화한 후 분기한다:

```bash
git checkout develop
git pull origin develop
git checkout -b feature/KAN-{번호}-{도메인}-{설명}
```

브랜치 컨벤션:
```
{타입}/{이슈키}-{도메인코드}-{간략설명}
```
- 타입: `feature`(신규), `fix`(버그), `refactor`(리팩토링)
- 도메인코드: `auth` `usr` `gth` `app` `loc` `adm`

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

---

## 참고 agent

- `.claude/agent/spec-reader.md`
- `.claude/agent/create-api.md`
- `.claude/agent/backend-reviewer.md`
