# Jira 컨벤션

이 문서는 이 프로젝트에서 Jira 이슈 생성, 업데이트, 상태 전환 시 따라야 할 규칙을 정의한다.

---

## 1. 이슈 생성 규칙

- projectKey: `KAN`
- issueTypeName: `Feature`
- assignee: `atlassianUserInfo`로 조회한 현재 로그인 사용자 accountId
- Start date(`customfield_10015`): 오늘 날짜 (YYYY-MM-DD)
- `contentFormat: adf` 를 반드시 사용한다 — markdown 포맷은 한글 인코딩이 깨지므로 사용하지 않는다

---

## 2. 이슈 description ADF 템플릿

`description` 필드에 아래 ADF 구조를 사용한다. 각 섹션의 `text` 값만 채워 넣는다.

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

---

## 3. 이슈 요약 출력 형식

Jira 이슈 작성 전 아래 형식으로 내용을 정리한다 (KAN-1 템플릿 기준):

```
[설명]
- 어떤 기능인지 간단히 작성

[요구사항]
- 무엇을 해야 하는지
- 제약 조건
- 인증 필요 여부

[API]
- Method / URL

[Request]
- 필드 목록 / 타입 / 필수 여부

[Response]
- 반환 필드 / 상태값

[DB]
- 사용 테이블 / 연관 관계

[예외]
- 케이스별 상태 코드
```

---

## 4. Jira 링크 형식

PR, 커밋, 코멘트 등에서 Jira 이슈를 참조할 때:

```
[{이슈키}](https://whatsuphouse.atlassian.net/browse/{이슈키})
```

예시: `[KAN-43](https://whatsuphouse.atlassian.net/browse/KAN-43)`

---

## 5. 금지 사항

- markdown contentFormat 사용 금지 (한글 깨짐)
- 불명확한 내용을 추측해서 이슈에 기재하지 않는다 — "문서에 명시 없음"으로 표기
