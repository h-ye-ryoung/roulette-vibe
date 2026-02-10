# CLAUDE.md

---

## 0. 프로젝트 개요

- Point Roulette (포인트 룰렛)
- 일일 예산/1일1회 제약을 동시성 환경에서도 정확히 지키는 '포인트 룰렛' 서비스를 풀스택으로 구현 및 배포
- 상세 명세: `docs/SPEC.md` 참조

---

## 1. 프로젝트 구성

- **backend/**: Spring Boot(Kotlin) API + Swagger
- **frontend/**: 사용자 웹 (룰렛/포인트/상품/주문)
- **admin/**: 어드민 웹 (예산/상품CRUD/취소-환불/룰렛취소)
- **mobile/**: Flutter(WebView) — 사용자 웹 렌더링
- **docs/**: 제출/명세/로그 문서

> **중요:** 사용자 웹과 어드민 웹은 **완전히 분리된 앱**으로 운영한다.

---

## 2. 핵심 요구사항

- 1일 1회 룰렛 (유저별)
- 일일 예산 100,000p (소진 시 지급 불가)
- 포인트는 100~1,000p 범위에서 랜덤 지급
- 포인트 유효기간: 지급일 + 30일 (만료 포인트 사용 불가)
- 포인트로 상품 구매 / 주문 취소 시 환불 / 룰렛 취소 시 회수

---

## 3. 확정 정책 (PDP)



| # | 항목 | 확정안 |
|---|---|---|
| PDP-1 | 예산 리셋 | **Lazy 리셋** — 요청 시 `budget_date`와 오늘(KST) 비교. 날짜가 다르면 새 행 생성. 스케줄러 불필요 |
| PDP-2 | 포인트 만료 | **동적 필터** — `expires_at > NOW()` 조건으로 유효분만 사용. 배치 불필요 |
| PDP-3 | 포인트 차감 순서 | **FIFO** — `expires_at ASC` 순. 만료 임박 포인트 먼저 소진 |
| PDP-4 | 환불 포인트 유효기간 | **원래 포인트 복원** — balance 되돌림. 이미 만료된 포인트는 복원되어도 사용 불가 |
| PDP-5 | 룰렛 취소 시 예산 복구 | **당일만 복구** — 취소일(KST) = 지급일이면 예산 복구. 다른 날이면 복구 안 함. 이미 사용된 포인트는 회수하지 않음 (balance만 확인) |
| PDP-6 | 예산 < 당첨금 | 랜덤 금액 결정 후 `remaining >= amount` 조건 차감. 실패 시 "예산 소진". 잔여 100p 미만이면 소진 상태 |

---

## 4. 동시성/정합성 규칙 (가장 중요)

- 중복 참여 방지: `UNIQUE(user_id, spin_date)` + INSERT 시도 → `DataIntegrityViolationException` 캐치
- 예산 원자적 차감: `UPDATE daily_budget SET remaining = remaining - :amount WHERE remaining >= :amount`
- 재고 원자적 차감: `UPDATE product SET stock = stock - 1 WHERE id = :id AND stock > 0`
- 포인트 차감: `SELECT ... FOR UPDATE` + FIFO balance 차감
- 예산 부족 시 지급은 실패(예산 소진)로 처리하며, remaining이 음수가 되면 실패이다.

> **주의:** "메모리 변수/프론트 체크"로 예산/참여 제한을 막지 않는다. DB가 진실의 원천이다.

### 트랜잭션 경계

| 작업 | 단일 트랜잭션 내 포함 |
|---|---|
| 룰렛 참여 | RouletteHistory INSERT + DailyBudget UPDATE + PointLedger INSERT |
| 상품 주문 | Order INSERT + PointLedger balance UPDATE(N건) + OrderPointUsage INSERT(N건) + Product stock UPDATE |
| 주문 취소 | Order 상태 변경 + PointLedger balance 복원(N건) + Product stock 복원 |
| 룰렛 취소 | RouletteHistory 상태 변경 + PointLedger balance 0 처리 + (당일이면) DailyBudget 복구 |

---

## 5. 시간 기준 (필수)

- '하루'의 기준은 **KST(Asia/Seoul)** 고정.
- 1일 1회/일일 예산/만료일 계산 등 모든 날짜 계산은 KST 기준으로 처리.
- 자정 전후 요청: 트랜잭션 시작 시점의 KST 날짜 사용.

---

## 6. 기술 스택 (SSOT)

### Backend (확정)
- **Spring Boot 3.5.0** / **Kotlin 2.0.21** / **Java 21 LTS**
- Build: **Gradle 8.x (Kotlin DSL)** — Wrapper로 관리
- ORM: **Spring Data JPA** (Hibernate)
- Auth: **Spring Security** + **DB 기반 토큰** (웹: HTTP 세션, 모바일: UUID 토큰 / 닉네임 자동 로그인)
- API 문서: **springdoc-openapi-starter-webmvc-ui** (Swagger UI)
- 검증: **spring-boot-starter-validation** (Jakarta Validation)
- 직렬화: **jackson-module-kotlin** + **kotlin-reflect**
- DB: **PostgreSQL 16** (로컬: Docker Compose / 배포: Neon)
- 테스트: **JUnit 5** + **MockK**

### Frontend / Admin
- React 18+ / TypeScript / Vite / TanStack Query / Tailwind

### Mobile
- Flutter(WebView)

### Infra
- GitHub Actions(backend CI/CD 필수)
- Vercel(frontend/admin 배포)
- Render(backend 배포), Neon(DB)

### 배포 아키텍처 (Production)

#### GitHub Actions CI/CD
- **워크플로우 파일**: `.github/workflows/backend-deploy.yml`
- **트리거**: `main` 브랜치 push (backend 폴더 변경 시)
- **빌드 환경**: Ubuntu Latest + JDK 21 (Temurin)
- **빌드 도구**: Gradle 8.x (Kotlin DSL)
- **파이프라인**:
  1. **Build & Test**: `./gradlew build` + `./gradlew test`
  2. **Artifact Upload**: JAR 파일 (1일 보관)
  3. **Deploy**: Render Deploy Hook 호출

#### Render (Backend Hosting)
- **플랜**: Free Tier
- **리전**: Oregon (US West)
- **런타임**: Java 21
- **빌드 명령**: `cd backend && ./gradlew build -x test`
- **시작 명령**: `cd backend && java -jar build/libs/*.jar`
- **Health Check**: `/actuator/health`
- **환경변수** (GitHub Secrets 관리):
  - `SPRING_PROFILES_ACTIVE=prod`
  - `DATABASE_URL` (Neon 연결 문자열)
  - `DATABASE_USERNAME` (수동 설정)
  - `DATABASE_PASSWORD` (수동 설정)
  - `ADMIN_NICKNAMES` (수동 설정)
  - `DAILY_BUDGET_DEFAULT=100000`
  - `SERVER_PORT=8080`
- **HTTPS**: 자동 제공 (Render 기본 지원)
- **도메인**: `*.onrender.com` (무료 서브도메인)

#### Neon PostgreSQL (Database)
- **플랜**: Free Tier
- **리전**: Oregon (US West)
- **버전**: PostgreSQL 16
- **데이터베이스명**: `roulette`
- **유저명**: `roulette_user`
- **연결 방식**: Render 환경변수로 자동 연결

#### 배포 워크플로우
```
1. 코드 push to main
   ↓
2. GitHub Actions 트리거
   ↓
3. 빌드 & 테스트 (JUnit 5)
   ↓
4. JAR 빌드 성공
   ↓
5. Render Deploy Hook 호출
   ↓
6. Render: JAR 다운로드 & 실행
   ↓
7. Health Check 통과
   ↓
8. 배포 완료 (HTTPS 자동 활성화)
```

#### 필수 GitHub Secrets
- `RENDER_DEPLOY_HOOK`: Render 배포 웹훅 URL (수동 설정 필요)

---

## 6.5. 로그인 인증 방식 (통일된 시스템)

### 단일 인증 시스템
- **웹 브라우저**: DB 기반 UUID 토큰 (X-Session-ID 헤더)
- **모바일 WebView**: DB 기반 UUID 토큰 (X-Session-ID 헤더)

### 동작 방식
1. **로그인**: UUID 토큰 생성 → DB 저장 (30일 유효) → 응답에 sessionId 포함
2. **클라이언트**: sessionId를 localStorage에 저장
3. **API 요청**: axios 인터셉터가 X-Session-ID 헤더로 전송
4. **인증**: X-Session-ID 헤더 확인 → DB 조회 → SecurityContext 설정

### 변경 이유
- 웹/모바일 인증 방식 통일로 유지보수 용이
- iOS/Android WebView 쿠키 이슈 완전 해결
- HttpSession은 fallback으로만 유지 (실제로는 사용 안 됨)

---

## 7. API 경로 규칙

| 경로 | 접근 권한 | 설명 |
|---|---|---|
| `/api/auth/**` | 누구나 | 로그인 등 인증 |
| `/api/user/**` | role=USER만 | 룰렛, 포인트, 상품 조회, 주문 생성/조회 (취소 불가) |
| `/api/admin/**` | role=ADMIN만 | 대시보드, 예산, 상품CRUD(생성/수정/삭제), 주문 취소/환불, 룰렛 취소 |

### 응답 포맷

```json
// 성공
{ "success": true, "data": { ... } }

// 실패
{ "success": false, "error": { "code": "ERROR_CODE", "message": "..." } }
```

### 에러 코드 (구분 가능)

`ALREADY_PARTICIPATED`, `BUDGET_EXHAUSTED`, `INSUFFICIENT_POINTS`, `PRODUCT_NOT_FOUND`, `PRODUCT_OUT_OF_STOCK`, `PRODUCT_HAS_ORDERS`, `ORDER_NOT_FOUND`, `ORDER_ALREADY_CANCELLED`, `ROULETTE_NOT_FOUND`, `ROULETTE_ALREADY_CANCELLED`, `FORBIDDEN`, `UNAUTHORIZED`

---

## 8. 엣지 케이스 처리 규칙

| 케이스 | 처리 |
|---|---|
| 자정(KST) 전후 요청 | 트랜잭션 시작 시점의 KST 날짜 사용 |
| 어드민 예산 변경 | **다음 날부터 적용**. 당일 remaining 변경 안 함 |
| 룰렛 취소 시 포인트 일부 사용 | 남은 포인트만 회수 (balance를 0으로). 이미 사용된 포인트는 그대로 유지 |
| 주문 취소 후 포인트 만료 상태 | balance 복원하지만 사용 불가 (만료 상태) |
| 예산 잔여 100p 미만 | 실질적 소진 상태. 룰렛 참여 시 `BUDGET_EXHAUSTED` |
| 상품 삭제 시 주문 내역 존재 | 삭제 거부 (`PRODUCT_HAS_ORDERS`). 주문이 없어야 삭제 가능 |

---

## 9. 코딩 컨벤션

### Backend (Kotlin)
- Controller: 요청/응답 매핑 + Service 호출만 (비즈니스 로직 금지)
- Service: 트랜잭션/정합성/동시성 처리의 중심
- Entity <-> DTO 분리 (Entity를 API로 직접 노출 금지)
- 동시성 로직은 **코드에서 명확히 보이게**(락/조건부 업데이트/유니크 제약)

### Frontend/Admin
- 컴포넌트는 page / feature / shared 단위로 분리
- API 호출은 TanStack Query로 통일
- 로딩/에러/빈 상태를 화면에서 반드시 처리

---

## 10. 테스트 정책

- 핵심 로직은 단위 테스트 우선. 특히:
  - 룰렛 참여(중복/예산)
  - 주문/환불
  - 포인트 만료
- 동시성은 단일 스레드 테스트로 끝내지 말고, **동시 요청 시나리오**를 반드시 포함한다.

### 필수 테스트 시나리오(체크리스트)

| # | 시나리오 | 기대 결과 |
|---|---|---|
| T-1 | 동일 유저 10개 동시 룰렛 요청 | 1건만 성공 |
| T-2 | 100명 동시 룰렛 (예산 100,000p) | 총 지급액 <= 100,000p |
| T-3 | 예산 500p 남은 상태에서 10명 동시 요청 | 최대 5명만 성공 |
| T-4 | 동일 상품(재고 1) 3명 동시 주문 | 1건만 성공 |
| T-5 | 만료 포인트로 주문 시도 | INSUFFICIENT_POINTS |
| T-6 | 주문 취소 후 포인트 잔액 복원 | balance 정확히 복원 |
| T-7 | 룰렛 취소 시 포인트 부분 사용 | 남은 포인트(balance)만 회수, 사용된 포인트는 유지 |

### 커버리지 목표
- 핵심 비즈니스 로직: 90% 이상
- 기타 새 코드: 80% 이상

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

### 필수 환경변수 목록

| 변수 | 용도 | 로컬 기본값 |
|---|---|---|
| `DATABASE_URL` | PostgreSQL 접속 URL | `jdbc:postgresql://localhost:5432/roulette` |
| `DATABASE_USERNAME` | DB 유저 | `postgres` |
| `DATABASE_PASSWORD` | DB 비밀번호 | `postgres` |
| `ADMIN_NICKNAMES` | 어드민 닉네임 목록 (콤마 구분) | `admin` |
| `DAILY_BUDGET_DEFAULT` | 기본 일일 예산 (선택, 기본값 100000) | `100000` |

### 프로파일 전략

| 프로파일 | DB | 용도 |
|---|---|---|
| `local` (기본) | Docker PostgreSQL 16 | 로컬 개발 |
| `prod` | Neon PostgreSQL | 배포 |

### 프론트엔드 개발 시 백엔드 API 연동 규칙 (필수)

- **로컬 프론트엔드는 배포된 백엔드 API를 사용한다.**
- 로컬에서 백엔드 서버를 실행하지 않는다. (백엔드 변경 사항이 없으면)
- 프론트엔드 개발 시 `.env.local`에 배포된 백엔드 URL 설정:
  ```
  VITE_API_BASE_URL=https://roulette-backend-upmn.onrender.com
  ```

---

## 12. 문서 / 로그 규칙 (핵심)

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

---

### SPEC.md / 명세 검증
- 명세는 `clarify → 사용자 답변 확정 → /ccpp:spec` 순서로 작성한다.
- 개발 기준 문서는 `docs/SPEC.md`로 관리한다.
- 구현 완료 후 `/ccpp:spec-verify`로 실제 구현이 명세와 일치하는지 검증한다.

---

## 13. 기본 작업 순서 규칙

> **작업 실행 규칙**:
> - 태스크는 기능 단위로 잘게 쪼개어 진행한다. 한 번에 여러 기능을 동시에 구현하지 않고, 기능 하나를 완료한 뒤 다음으로 넘어간다.
> - **단계별 확인 필수**: 각 단계 완료 후 요약을 제시하고, 사용자 확인을 받은 뒤 다음 단계로 진행한다. 임의로 다음 단계로 넘어가지 않는다.

---

## 14. SSOT(Single Source of Truth) 원칙

1. **명세 참조 필수**: 구현 시 반드시 `docs/SPEC.md`를 참조한다. SPEC.md에 정의되지 않은 사항을 임의로 구체화하거나 추가하지 않는다.
2. **문서 역할 분리**:
   - `CLAUDE.md` — 설계 원칙, 정책, 규칙 (What & Why)
   - `docs/SPEC.md` — 구현 명세 (How: 엔티티, API, 화면, 테스트 상세)
   - 동일 정보를 두 문서에 중복 기술하지 않는다. CLAUDE.md는 정책 수준만 기술하고, 구체적 구현 명세는 SPEC.md에 위임한다.
3. **충돌 시 우선순위**: CLAUDE.md(정책) > SPEC.md(명세). 정책이 명세보다 우선한다.

---

## 15. 유지보수 규칙

- 작업 중 새로 발견된 규칙이나 반복되는 실수는 즉시 CLAUDE.md에 반영한다.
- 오래되었거나 상충되는 규칙은 제거하거나 수정한다.
- PR 리뷰 또는 회고 시 규칙을 점검하고 필요 시 업데이트한다.

---

## 16. 언어 규칙

* 본 프로젝트의 문서는 한국어로 작성한다.
* 코드, API 명세, 기술 용어는 필요 시 영어 사용

---

## 17. 어드민 웹 프론트엔드 (admin/)

### 17.1 기술 스택

| 항목 | 기술 | 버전 | 용도 |
|---|---|---|---|
| 프레임워크 | React | 18+ | UI 라이브러리 |
| 언어 | TypeScript | 5+ | 타입 안전성 (strict mode) |
| 빌드 도구 | Vite | 5+ | 빠른 개발 서버 |
| 라우팅 | React Router | v6 | SPA 라우팅 |
| 서버 상태 | TanStack Query | v5 | 데이터 페칭/캐싱 |
| 폼 처리 | React Hook Form | 7+ | 비제어 컴포넌트 패턴 |
| 폼 검증 | Zod | 3+ | 스키마 기반 검증 |
| 스타일링 | Tailwind CSS | 3+ | 유틸리티 CSS |
| UI 컴포넌트 | shadcn/ui | latest | Radix UI 기반 |
| 클라이언트 상태 | React Context | - | 인증 상태만 |
| 배포 | Vercel | - | 자동 배포/CDN |

### 17.2 개발 원칙

#### 1. 타입 안정성 (/typescript-advanced-types 스킬 활용)

**Generic 타입으로 API 응답 래핑**:
```typescript
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error?: ApiError;
}
```

**Discriminated Unions로 상태 관리**:
```typescript
export type QueryState<T> =
  | { status: 'loading' }
  | { status: 'success'; data: T }
  | { status: 'error'; error: Error };
```

**Utility Types로 타입 재사용**:
```typescript
export type ProductFormData = Pick<Product, 'name' | 'price' | 'stock'>;
export type ProductUpdateData = Omit<Product, 'id' | 'createdAt'>;
```

#### 2. UI 개발 (/ccpp:frontend 스킬 활용)

**빅테크 스타일 (Stripe, Vercel, Apple)**:
- 깔끔한 레이아웃과 넓은 여백
- 섬세한 그림자와 부드러운 애니메이션
- 명확한 타이포그래피 계층 구조

**일관된 디자인 시스템**:
- shadcn/ui 컴포넌트로 통일된 UI
- Tailwind CSS 클래스 순서 준수
- 색상 팔레트 일관성 유지

**접근성 준수 (ARIA)**:
- 모든 인터랙티브 요소에 적절한 ARIA 속성
- 키보드 네비게이션 지원
- 스크린 리더 호환성

#### 3. 코드 품질

**React Hook Form으로 비제어 컴포넌트 패턴**:
- 성능 최적화 (불필요한 리렌더링 방지)
- Zod 스키마 통합으로 타입 안전한 폼 검증

**TanStack Query로 선언적 데이터 페칭**:
- 자동 캐싱 및 재검증
- 낙관적 업데이트 (Optimistic Updates)
- 에러 처리 및 재시도 로직 내장

**shadcn/ui로 일관된 컴포넌트 재사용**:
- 합성 패턴 (Composition Pattern)
- 컴파운드 컴포넌트 (Compound Components)
- 접근성 기본 제공 (Radix UI 기반)

### 17.3 폴더 구조

```
admin/
├── src/
│   ├── api/                 # API 호출 함수
│   │   ├── client.ts        # Axios 인스턴스
│   │   ├── dashboard.ts
│   │   ├── budget.ts
│   │   ├── roulette.ts
│   │   ├── products.ts
│   │   └── orders.ts
│   ├── components/
│   │   ├── ui/              # shadcn/ui 컴포넌트
│   │   ├── layout/          # Layout, Sidebar, Header
│   │   └── shared/          # 공통 컴포넌트
│   ├── pages/               # 페이지 (6개)
│   ├── contexts/            # AuthContext
│   ├── hooks/               # Custom hooks
│   ├── types/               # TypeScript 타입
│   └── lib/                 # 유틸리티
├── .env.local               # 환경변수 (gitignore)
└── vercel.json              # Vercel 배포 설정
```

### 17.4 관련 문서

- **상세 명세**: `docs/ADMIN_SPEC.md`
- **배포 전략**: `docs/ADMIN_SPEC.md` 섹션 14
- **타입 전략**: `docs/ADMIN_SPEC.md` 섹션 15
- **UI 전략**: `docs/ADMIN_SPEC.md` 섹션 16

---

## 18. 사용자 웹 프론트엔드 (frontend/)

### 18.1 기술 스택

| 항목 | 기술 | 버전 | 용도 |
|---|---|---|---|
| 프레임워크 | React | 18+ | UI 라이브러리 |
| 언어 | TypeScript | 5+ | 타입 안전성 (strict mode) |
| 빌드 도구 | Vite | 5+ | 빠른 개발 서버 |
| 라우팅 | React Router | v6 | SPA 라우팅 |
| 서버 상태 | TanStack Query | v5 | 데이터 페칭/캐싱 |
| 폼 처리 | React Hook Form | 7+ | 비제어 컴포넌트 패턴 |
| 폼 검증 | Zod | 3+ | 스키마 기반 검증 |
| 스타일링 | Tailwind CSS | 3+ | 유틸리티 CSS |
| UI 컴포넌트 | shadcn/ui | latest | Radix UI 기반 |
| 클라이언트 상태 | React Context | - | 인증 상태만 |
| 날짜 처리 | date-fns | 4+ | 날짜 포맷팅 |
| 배포 | Vercel | - | 자동 배포/CDN |

### 18.2 디자인 원칙 (필수)

#### 1. 레이아웃 통일

**AppLayout 구조** (모든 페이지 공통):
```tsx
<div className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-purple-50">
  <Header title={title} />
  <main className="flex items-center justify-center min-h-[calc(100vh-7.5rem)]">
    <div className="w-full max-w-screen-sm px-4 py-4">
      {children}
    </div>
  </main>
  <BottomNav />
</div>
```

**핵심 규칙**:
- ✅ 모든 페이지는 `AppLayout`으로 래핑
- ✅ 상단바(`Header`) 고정: sticky, backdrop-blur
- ✅ 하단바(`BottomNav`) 고정: 4개 탭 네비게이션
- ✅ 중앙 정렬: `flex items-center justify-center`
- ✅ 컨테이너: `max-w-screen-sm` (모바일 우선)
- ✅ 여백: `px-4 py-4` (일관된 패딩)

#### 2. 색상 팔레트 (Purple-Pink 그라디언트)

**배경**:
```css
bg-gradient-to-br from-purple-50 via-pink-50 to-purple-50
```

**메인 그라디언트** (제목, 금액, 버튼):
```css
bg-gradient-to-r from-purple-600 to-pink-600
```

**그라디언트 텍스트**:
```css
bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent
```

**버튼**:
```css
bg-gradient-to-r from-purple-600 to-pink-600
hover:from-purple-700 hover:to-pink-700
```

**보조 색상**:
- 참여 완료/알림: `bg-gradient-to-r from-purple-100 to-pink-100`
- 만료 예정: `bg-gradient-to-r from-orange-100 to-red-100`
- 카드 배경: `bg-gradient-to-r from-purple-50/30 to-pink-50/30`

#### 3. 카드 스타일 (유리모피즘)

**기본 카드** (잔액, 정보 표시):
```tsx
<Card className="backdrop-blur-lg bg-white/70 border-white/20 shadow-xl">
  <CardHeader>
    <CardTitle className="text-center text-lg">{title}</CardTitle>
  </CardHeader>
  <CardContent className="space-y-4">
    <div className="text-center">
      <div className="text-5xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
        {value}
      </div>
    </div>
  </CardContent>
</Card>
```

**알림 배너** (참여 완료, 만료 예정):
```tsx
<div className="bg-gradient-to-r from-purple-100 to-pink-100 rounded-lg p-3 border border-purple-200">
  <div className="flex items-center justify-between">
    <div className="flex items-center gap-2">
      <span className="text-lg">✅</span>
      <p className="text-sm font-medium text-gray-700">{message}</p>
    </div>
    <p className="text-lg font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
      {value}
    </p>
  </div>
</div>
```

#### 4. 환영 메시지 (모든 페이지 상단)

**필수 패턴**:
```tsx
<div className="text-center space-y-1">
  <p className="text-lg font-semibold text-gray-800">
    <span className="bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
      {user?.nickname}
    </span>
    님, 환영합니다! 👋
  </p>
  <p className="text-sm text-gray-600">{description}</p>
</div>
```

#### 5. 여백 및 간격

**섹션 간 간격**:
```tsx
<div className="space-y-6">  // 페이지 전체
```

**카드 내부**:
```tsx
<CardContent className="space-y-4">  // 카드 내 요소
```

**목록 아이템**:
```tsx
<div className="space-y-2">  // 리스트 아이템 (컴팩트)
<div className="space-y-3">  // 리스트 아이템 (일반)
```

**환영 메시지/텍스트 그룹**:
```tsx
<div className="space-y-1">  // 텍스트 그룹
```

#### 6. 정보 밀도

**큰 숫자 (강조)**:
```tsx
text-5xl font-bold  // 포인트 잔액, 예산
text-4xl font-bold  // 룰렛 당첨 금액
```

**일반 금액**:
```tsx
text-lg font-bold   // 내역 금액
text-base font-bold // 작은 카드 금액
```

**보조 텍스트**:
```tsx
text-sm text-gray-600  // 설명 텍스트
text-xs text-gray-500  // 날짜, 부가 정보
```

#### 7. 페이지 일관성 체크리스트

새 페이지 추가 시 반드시 확인:
- [ ] `AppLayout`으로 래핑
- [ ] 환영 메시지 포함 (`{user?.nickname}님...`)
- [ ] Purple-Pink 그라디언트 사용
- [ ] 유리모피즘 카드 스타일
- [ ] `space-y-6` 섹션 간격
- [ ] 중앙 정렬 텍스트 (제목, 금액)
- [ ] 큰 숫자는 그라디언트 텍스트
- [ ] 로딩 상태: `<FullScreenLoading />`
- [ ] 빈 상태: 이모지 + 안내 문구

### 18.3 폴더 구조

```
frontend/
├── src/
│   ├── api/                 # API 호출 함수
│   │   ├── client.ts        # Axios 인스턴스
│   │   ├── auth.ts          # 인증 API
│   │   ├── roulette.ts      # 룰렛 API
│   │   ├── points.ts        # 포인트 API (Advanced Types)
│   │   ├── products.ts      # 상품 API
│   │   └── orders.ts        # 주문 API
│   ├── components/
│   │   ├── ui/              # shadcn/ui 컴포넌트
│   │   ├── layout/          # AppLayout, Header, BottomNav
│   │   ├── BudgetCard.tsx   # 예산 카드
│   │   ├── RouletteWheel.tsx # 룰렛 휠
│   │   └── LoadingSpinner.tsx # 로딩 애니메이션
│   ├── pages/               # 페이지 (5개)
│   │   ├── LoginPage.tsx    # 로그인
│   │   ├── RoulettePage.tsx # 룰렛 (홈)
│   │   ├── PointsPage.tsx   # 포인트 내역
│   │   ├── ProductsPage.tsx # 상품 목록
│   │   └── OrdersPage.tsx   # 주문 내역
│   ├── contexts/            # AuthContext
│   ├── hooks/               # Custom hooks
│   ├── types/               # TypeScript 타입
│   └── lib/                 # 유틸리티
├── .env.local               # 환경변수 (gitignore)
└── vercel.json              # Vercel 배포 설정
```

### 18.4 TypeScript Advanced Types 활용

포인트 내역 페이지에서 사용한 패턴을 다른 페이지에도 적용:

**Branded Types**:
```typescript
type ISODateTimeString = string & { readonly __brand: 'ISODateTime' };
```

**Discriminated Unions**:
```typescript
type Status = 'ACTIVE' | 'INACTIVE' | 'EXPIRED';
```

**Type Guards**:
```typescript
function isExpired(item: Item): item is Item & { expired: true } {
  return item.expired;
}
```

**Utility Types**:
```typescript
type PaginatedResponse<T> = {
  readonly items: readonly T[];
  readonly pageInfo: PageInfo;
};
```

### 18.5 배포 전략

**Vercel 자동 배포**:
- Git push 시 자동 배포
- 환경변수: `VITE_API_BASE_URL` (배포된 백엔드 URL)
- 프리뷰 배포: PR별 자동 생성
- Production: main 브랜치 머지 시

**로컬 개발**:
- 배포된 백엔드 API 사용 (`.env.local`)
- 로컬 백엔드 실행 불필요
- 실제 배포 환경과 동일한 조건 테스트

---

## 19. Codex MCP 사용 규칙

### 원칙
- **Codex를 호출하면 완료까지 기다린다**: Codex 작업이 느리거나 응답이 없다고 판단하여 임의로 중단하고 직접 구현하지 않는다.
- **Codex가 주도권을 가진다**: Codex에게 작업을 위임했다면, Codex가 작업을 완료할 때까지 기다린다.

### 금지 사항
- ❌ Codex 호출 후 "응답이 느리다"는 이유로 직접 코드 작성
- ❌ Codex 작업 중간에 "더 빠른 방법"으로 전환
- ❌ Codex 완료 전에 동일 작업을 수동으로 진행

### 허용 사항
- ✅ Codex 작업 완료 후 결과 확인 및 검증
- ✅ Codex 실패 시 재시도 또는 다른 접근 방법 선택
- ✅ Codex 작업 진행 상황 모니터링 (읽기 전용)

### 사용 시점
- 복잡한 테스트 작성 및 실행
- 여러 파일에 걸친 리팩토링
- 코드베이스 분석 및 패턴 탐색
- 자동화된 검증 및 테스트 실행

---

## 폴더 구조

```text
roulette-vibe/
├─ backend/
├─ frontend/          # 사용자용 웹
├─ admin/             # 어드민 웹
├─ mobile/            # Flutter WebView
├─ docs/
│  ├─ SPEC.md         # 확정 명세서
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