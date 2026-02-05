# HANDOFF.md — 세션 인계 문서

> **최종 갱신**: 2026-02-05T18:15:00+09:00

---

## 1. 완료된 작업

| # | 작업 | 상태 | 산출물 |
|---|---|---|---|
| 1 | 요구사항 분석 및 Planner 역할 수행 | 완료 | 서버 플랜 문서 (대화 내) |
| 2 | PDP(Policy Decision Point) 1~8 확정 | 완료 | `CLAUDE.md` §3 |
| 3 | PDP-8 역할 기반 접근 제어 정책 2차 수정 | 완료 | ADMIN/USER 상호 접근 금지(403) |
| 4 | `docs/SPEC.md` 작성 (v1.0 확정) | 완료 | 7개 엔티티, 18개 API, 12개 에러코드, 7개 동시성 테스트 시나리오 |
| 5 | `CLAUDE.md` 전면 업데이트 | 완료 | 확정 정책/동시성 규칙/API 경로/에러코드/환경변수 반영 |
| 6 | `docs/PROMPT.md` 대화 로그 기록 | 완료 | 커서: `2026-02-05T18:10:00+09:00` |
| 7 | `docs/HANDOFF.md` 인계 문서 작성 | 완료 | 세션 종료 시 최종 갱신 |

---

## 2. 남은 작업 (우선순위순)

| 우선순위 | 작업 | 비고 |
|---|---|---|
| **P0** | Phase 1: 백엔드 코어 구현 | Spring Boot + Kotlin + JPA + PostgreSQL |
| P1 | Phase 2: 백엔드 배포 | Render/Railway + Neon + GitHub Actions CI/CD |
| P2 | Phase 3: 프론트엔드(사용자) 구현 | React + TypeScript + Vite + TanStack Query + Tailwind |
| P2 | Phase 4: 어드민 웹 구현 | 사용자 웹과 별도 앱 |
| P3 | Phase 5: 프론트엔드/어드민 배포 | Vercel |
| P4 | Phase 6: 모바일(Flutter WebView) 구현 및 배포 | 사용자 웹 래핑 |

### Phase 1 세부 순서 (SPEC.md §11 기준)

1. Gradle/Spring Boot 프로젝트 초기화 (`backend/`)
2. Entity + JPA 매핑 (7개 엔티티)
3. Auth (닉네임 로그인 + JWT + Role 필터)
4. 룰렛 기능 (중복 방지 + 예산 원자적 차감)
5. 포인트 조회/차감 (FIFO + 만료 필터)
6. 상품 CRUD + 주문/취소
7. 어드민 API (예산/대시보드/취소)
8. Swagger(OpenAPI 3) 설정
9. 동시성 테스트 (T-1 ~ T-7)

---

## 3. 확정된 설계 결정 요약

| 결정 | 내용 | 근거 |
|---|---|---|
| PDP-1 Lazy 예산 리셋 | 요청 시 budget_date vs 오늘(KST) 비교, 날짜 다르면 새 행 | 스케줄러 불필요, 단순성 |
| PDP-2 동적 만료 필터 | `expires_at > NOW()` 쿼리 조건 | 배치 불필요, 실시간 정확성 |
| PDP-3 FIFO 차감 | `expires_at ASC` 순 소진 | 만료 임박 포인트 우선 소비 |
| PDP-4 원래 포인트 복원 | balance 되돌림, 만료 시 사용 불가 | 유효기간 정합성 유지 |
| PDP-5 당일만 예산 복구 | 취소일(KST) = 지급일이면 복구 | 일일 예산 정합성 |
| PDP-6 예산 조건부 차감 | `remaining >= amount` 조건 UPDATE | 원자적 처리 |
| PDP-7 닉네임 = 유저 ID | 자동 생성/로그인, 회원가입 없음 | 요구사항 간소화 |
| PDP-8 역할 완전 분리 | ADMIN↔USER 상호 403, /api/auth/** 공개 | 기능 혼용 방지 |
| 동시성 전략 | UNIQUE 제약 + 조건부 UPDATE + SELECT FOR UPDATE | DB가 진실의 원천 |
| 시간 기준 | KST(Asia/Seoul) 고정 | 모든 날짜 계산 통일 |

---

## 4. 미해결 이슈 / 보류 사항

| 이슈 | 상태 | 비고 |
|---|---|---|
| Docker Compose 파일 미작성 | 보류 | `compose.yml` — 로컬 PostgreSQL용, Phase 1 시작 시 작성 |
| `.env.example` 미작성 | 보류 | Phase 1 시작 시 작성 |
| GitHub Actions 워크플로우 미작성 | 보류 | Phase 2(배포) 시 작성 |
| 코드 구현 0% | - | 아직 코드 작성 시작 안 함 |

---

## 5. 현재 막힌 지점

**막힌 지점 없음.** 기획/명세 단계가 완료되어 바로 구현 시작 가능.

---

## 6. 다음 세션 첫 3개 액션

### Action 1: Spring Boot + Kotlin 프로젝트 초기화
- `backend/` 디렉토리에 Gradle(Kotlin DSL) 프로젝트 생성
- 의존성: Spring Web, Spring Data JPA, PostgreSQL Driver, Spring Security, Swagger(springdoc-openapi), JWT 라이브러리
- `application.yml` 기본 설정 (프로파일: local/prod 분리)
- `compose.yml` (PostgreSQL 15 컨테이너)
- `.env.example` 작성

### Action 2: Entity + JPA 매핑
- 7개 엔티티 작성: `User`, `DailyBudget`, `RouletteHistory`, `PointLedger`, `Product`, `Order`, `OrderPointUsage`
- UNIQUE 제약 조건: `RouletteHistory(user_id, spin_date)`, `DailyBudget(budget_date)`
- `@NamedNativeQuery` 또는 JPQL로 동시성 쿼리 준비
- Flyway 또는 Hibernate DDL Auto로 스키마 관리

### Action 3: Auth 구현 (닉네임 로그인 + JWT + Role 필터)
- `POST /api/auth/login` — 닉네임으로 로그인, 없으면 자동 생성
- JWT 발급 (nickname, role 포함)
- `SecurityFilterChain` 설정: `/api/auth/**` 허용, `/api/user/**` → USER만, `/api/admin/**` → ADMIN만
- `ADMIN_NICKNAMES` 환경변수 기반 role 부여

---

## 7. 참조 문서

| 문서 | 경로 | 용도 |
|---|---|---|
| 프로젝트 규칙 | `CLAUDE.md` | 전체 정책/규칙/컨벤션 |
| 확정 명세서 | `docs/SPEC.md` | 엔티티/API/화면/테스트 상세 |
| 대화 로그 | `docs/PROMPT.md` | 원문 프롬프트/응답 기록 |