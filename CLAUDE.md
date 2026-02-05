# CLAUDE.md

---

## 0. 프로젝트 개요


- Point Roulette (포인트 룰렛)
- 일일 예산/1일1회 제약을 동시성 환경에서도 정확히 지키는 ‘포인트 룰렛’ 서비스를 풀스택으로 구현 및 배포

---
## 1. 프로젝트 구성
- **backend/**: Spring Boot(Kotlin) API + Swagger
- **frontend/**: 사용자 웹 (룰렛/포인트/상품/주문)
- **admin/**: 어드민 웹 (예산/상품CRUD/취소-환불/룰렛취소)
- **mobile/**: Flutter(WebView) — 사용자 웹 렌더링
- **docs/**: 제출/명세/로그 문서

> **중요:** 사용자 웹과 어드민 웹은 **완전히 분리된 앱**으로 운영한다.
> 
---

## 2. 핵심 요구사항
- 1일 1회 룰렛 (유저별)
- 일일 예산 100,000p (소진 시 지급 불가)
- 포인트는 100~1,000p 범위에서 랜덤 지급
- 포인트 유효기간: 지급일 + 30일 (만료 포인트 사용 불가)
- 포인트로 상품 구매 / 주문 취소 시 환불 / 룰렛 취소 시 회수

---

## 3.동시성/정합성 규칙 (가장 중요)
- 중복 참여 방지: `(userId, date)` 유니크 제약 + 트랜잭션으로 하루 1회만 성공하게 한다.
- 예산 초과 방지: 예산 차감은 조건부 원자 업데이트(remaining >= win) 또는 row lock으로만 처리한다.
- 예산 부족 시 지급은 실패(예산 소진)로 처리하며, remaining이 음수가 되면 실패이다.

> **주의:** “메모리 변수/프론트 체크”로 예산/참여 제한을 막지 않는다. DB가 진실의 원천이다.

---

## 4.시간 기준 (필수)
- ‘하루’의 기준은 **KST(Asia/Seoul)** 고정.
- 1일 1회/일일 예산/만료일 계산 등 모든 날짜 계산은 KST 기준으로 처리.

## 5. 기술 스택

### Backend
- Spring Boot 3.x / Kotlin / JPA(Hibernate) / Swagger(OpenAPI 3)
- DB: PostgreSQL(개발: Docker, 배포: Neon 등)

### Frontend / Admin
- React 18+ / TypeScript / Vite / TanStack Query / Tailwind

### Mobile
- Flutter(WebView)

### Infra
- GitHub Actions(backend CI/CD 필수)
- Vercel(frontend/admin 배포)
- Render/Railway(backend 배포), Neon(DB)

---

## 6. 실행 커맨드(프로젝트에 맞게 유지/업데이트)
> 아래 커맨드는 실제 repo에 맞게 반드시 최신 상태로 유지한다.

### Backend (예시)
- Run: `./gradlew bootRun`
- Test: `./gradlew test`
- Build: `./gradlew clean build`

### Frontend/Admin (예시)
- Install: `npm i`
- Dev: `npm run dev`
- Build: `npm run build`
- Lint(선택): `npm run lint`

### Mobile (예시)
- Run: `flutter run`
- Build APK: `flutter build apk`

---

## 7. 코딩 컨벤션

### Backend (Kotlin)
- Controller: 요청/응답 매핑 + Service 호출만 (비즈니스 로직 금지)
- Service: 트랜잭션/정합성/동시성 처리의 중심
- Entity ↔ DTO 분리 (Entity를 API로 직접 노출 금지)
- 동시성 로직은 **코드에서 명확히 보이게**(락/조건부 업데이트/유니크 제약)

### Frontend/Admin
- 컴포넌트는 page / feature / shared 단위로 분리
- API 호출은 TanStack Query로 통일
- 로딩/에러/빈 상태를 화면에서 반드시 처리

---

## 8. API 응답/에러 규칙(실수 방지)
- 성공/실패 응답 포맷은 일관되게 유지한다.
- 실패 응답은 최소:
  - `code`(에러 코드)
  - `message`
  - 필요 시 `details`
- “오늘 이미 참여”, “예산 부족”, “포인트 부족/만료”는 **서로 구분 가능한 형태**로 반환한다.

---

## 9. 테스트 정책(핵심만)
- 핵심 로직은 단위 테스트 우선. 특히:
  - 룰렛 참여(중복/예산)
  - 주문/환불
  - 포인트 만료
- 동시성은 단일 스레드 테스트로 끝내지 말고, **동시 요청 시나리오**를 반드시 포함한다.

### 필수 테스트 시나리오(체크리스트)
- 동일 유저 동시 룰렛 요청 → 1회만 성공
- 다중 유저 동시 룰렛 요청 → 총 지급이 예산 초과 금지
- 예산 부족 상태 → 지급 실패
- 만료 포인트 → 사용 불가
- 주문 시 포인트 부족/만료 포함 → 결제 실패
- 주문 취소/룰렛 취소 → 정책대로 환불/회수

---

## 10. 문서 / 로그 규칙 (핵심)

### PROMPT.md (원문 로그, 가공 금지)
- AI와 주고받은 프롬프트/응답을 **가공 없이 원문 그대로** 기록한다.
- 요약, 편집, 재작성은 허용하지 않는다.
- 기록은 단일 파일 `docs/PROMPT.md`에서 관리한다.
- 중복 기록을 방지하기 위해 **커서 메타 방식**을 사용한다.

PROMPT.md 하단에는 마지막 기록 시점을 나타내는 커서를 둔다.

예시:
<!-- LAST_LOG_CURSOR: 2026-02-05T14:32:10+09:00 -->

기록 규칙:
- 새로운 기록 시 **이 커서 이후에 발생한 대화만 append**한다.
- 이미 기록된 대화는 다시 포함하지 않는다.
- 기록 완료 후 커서를 현재 시점으로 업데이트한다.

> 권장 기록 요청 문구:
>LAST_LOG_CURSOR 이후의 프롬프트/응답만 가공 없이 docs/PROMPT.md에 append하고 커서를 현재 시점으로 업데이트해줘.

---

### HANDOFF.md (세션 인계 문서)
- 세션 종료, IDE 변경, 작업 중단 후에도 즉시 맥락을 이어가기 위한 문서이다.
- `docs/HANDOFF.md`로 관리한다.

반드시 포함할 항목:
- 현재 진행 상태
- 확정된 설계 결정
- 미해결 이슈 / 보류 사항
- 다음 Claude 액션

갱신 시점:
- 세션 종료 시
- 큰 기능 단위 완료 시
- IDE 또는 개발 환경 변경 전

> `/ccpp:handoff` 커맨드 사용을 권장한다.

---

### SPEC.md / 명세 검증
- 명세는 `clarify → 사용자 답변 확정 → /ccpp:spec` 순서로 작성한다.
- 개발 기준 문서는 `docs/SPEC.md`로 관리한다.
- 구현 완료 후 `/ccpp:spec-verify`로 실제 구현이 명세와 일치하는지 검증한다.

---


## 11. 환경 변수 / 시크릿 정책 (중요)
- 모든 민감 정보(DB URL, API 키 등)는 코드에 하드코딩하지 않는다.
- 로컬 환경:
  - `.env` 또는 `application-local.yml` 사용
  - 해당 파일은 gitignore 처리한다.
- 배포 환경:
  - GitHub Secrets
  - Vercel Environment Variables
- 저장소에는 `.env.example`만 커밋한다.

---

## 12. 기본 작업 순서 원칙

1. clarify 실행
2. 사용자 답변 확정
3. `/ccpp:spec`으로 명세서 작성
4. 백엔드 구현
5. 백엔드 배포
6. 프론트엔드 / 어드민 구현
7. 프론트엔드 배포
8. 모바일(Flutter) 구현
9. 모바일 배포

---

## 13. 유지보수 규칙
- 작업 중 새로 발견된 규칙이나 반복되는 실수는 즉시 CLAUDE.md에 반영한다.
- 오래되었거나 상충되는 규칙은 제거하거나 수정한다.
- PR 리뷰 또는 회고 시 규칙을 점검하고 필요 시 업데이트한다.


---

## 14. 언어 규칙

* 본 프로젝트의 문서는 한국어로 작성한다.
* 코드, API 명세, 기술 용어는 필요 시 영어 사용

---

## 폴더 구조

```text
roulette-vibe/
├─ backend/
├─ frontend/          # 사용자용 웹
├─ admin/             # 어드민 웹
├─ mobile/            # Flutter WebView
├─ docs/
│  ├─ SPEC.md
│  ├─ PROMPT.md
│  ├─ HANDOFF.md
│  └─ AI_REPORT.md
├─ .github/
│  └─ workflows/      # backend CI/CD (필수)
├─ .claude/
│  └─ commands/       # clarify, log, handoff 등 커맨드
├─ compose.yml        # 로컬 DB(Postgres)용(권장)
├─ README.md
└─ CLAUDE.md
```


