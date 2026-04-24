# /implement

구글 드라이브 기획 문서를 읽고, Jira 이슈를 생성하고, 코드를 구현하고, 검토 후 완료 처리까지 자동으로 수행한다.

## 사용법

```
/implement {구현할 기능 설명}
```

---

## 0단계 — 사전 연결 체크

작업 시작 전, 아래 세 가지 연결 상태를 먼저 확인한다:
- Google Drive MCP: `search_files`로 테스트 조회
- Jira MCP: `atlassianUserInfo`로 현재 사용자 조회
- gh CLI: `gh auth status`로 GitHub 인증 확인

⚠️ 하나라도 실패하면 **즉시 작업을 중단**하고 보고한다. 이후 단계는 절대 진행하지 않는다.

---

## 1단계 — 문서 읽기 (spec-reader)

`.claude/agent/spec-reader.md` 역할을 수행한다.

- 검색 결과 스니펫이 충분하면 `read_file_content`를 호출하지 않는다
- 스니펫이 불충분할 때만 전문을 읽는다

---

## 2단계 — Jira 이슈 생성

`.claude/rules/jira.md` 규칙을 따라 이슈를 생성한다.

---

## 3단계 — 브랜치 생성

`.claude/rules/git.md` 브랜치 규칙을 따른다.

```bash
git checkout develop
git pull origin develop
git checkout -b {타입}/{이슈키}-{도메인코드}-{간략설명}
```

---

## 4단계 — 코드 구현 (create-api)

`.claude/agent/create-api.md` 역할을 수행한다.

- 구현 전, 동일/인접 도메인의 기존 코드를 읽어 스타일을 파악한다
  - **Controller, Service, Repository, DTO 각 1개 파일씩만** 읽는다
  - 파일이 길면 상단 60줄만 읽는다
- 이미 구현된 코드가 있으면 규칙 위반 여부만 확인한다

---

## 5단계 — 검토 (backend-reviewer)

`.claude/agent/backend-reviewer.md` 역할을 수행한다. 문제가 있으면 즉시 수정한다.

---

## 6단계 — Jira 완료 처리

이슈에 검토 결과 코멘트 추가 후 상태를 "완료"로 전환한다.

---

## 7단계 — 커밋 & PR (pr-creator)

`.claude/agent/pr-creator.md` 역할을 수행한다.
