# HANDOFF.md — 세션 인계 문서

> **최종 갱신**: 2026-02-06T21:00:00+09:00

---

## 1. 완료된 작업

| # | 작업 | 상태 | 산출물 |
|---|---|---|---|
| 1 | 요구사항 분석 및 PDP 1~8 확정 | 완료 | `CLAUDE.md` §3 |
| 2 | API 명세 확정 | 완료 | `SPEC.md` v1.1 (22개 API) |
| 3 | Gradle 프로젝트 초기화 | 완료 | `backend/build.gradle.kts`, Gradle Wrapper 8.12 |
| 4 | Docker Compose 작성 | 완료 | `compose.yml` — PostgreSQL 16 컨테이너 |
| 5 | 설정 파일 작성 | 완료 | `application.yml`, `application-local.yml`, `application-prod.yml` |
| 6 | 환경변수 예제 작성 | 완료 | `backend/.env.example` |
| 7 | 메인 애플리케이션 작성 | 완료 | `RouletteApplication.kt` |
| 8 | Entity + JPA 매핑 (7개) | 완료 | User, DailyBudget, RouletteHistory, PointLedger, Product, Order, OrderPointUsage |
| 9 | Repository 인터페이스 + 동시성 쿼리 | 완료 | `decrementRemaining`, `decrementStock`, `findAvailableByUserIdForUpdate` |
| 10 | 세션 기반 Auth 구현 | 완료 | AuthController, AuthService, CustomUserDetailsService, SecurityConfig |
| 11 | 예외 처리 체계 구축 | 완료 | BusinessException 계층, GlobalExceptionHandler, ApiResponse 래퍼 |
| 12 | **룰렛 기능 (Step 11)** | ✅ 완료 | RouletteService, RouletteController, 중복 방지 로직 |
| 13 | **포인트 기능 (Step 12)** | ✅ 완료 | PointService, PointController, 잔액/내역 조회, 만료 예정 구분 |
| 14 | **상품 기능 (Step 13)** | ✅ 완료 | ProductService, ProductController, 재고 필터링 |
| 15 | **주문 기능 (Step 14)** | ✅ 완료 | OrderService, OrderController, FIFO 포인트 차감 |
| 16 | **어드민 API (Step 15)** | ✅ 완료 | AdminService, AdminController, 대시보드/예산/상품/주문/룰렛 관리 (12개 API) |
| 17 | **동시성 테스트 (Step 16)** | ✅ 완료 | ConcurrencyTest.kt (T-1~T-7 모두 통과) |
| 18 | 대화 로그 기록 | 완료 | `docs/PROMPT.md` (커서: `2026-02-06T20:59:00+09:00`) |

---

## 2. 남은 작업 (우선순위순)

### Phase 1: 백엔드 코어 구현 ✅ 완료

| 우선순위 | 작업 | 상태 |
|---|---|---|
| ~~P0~~ | ~~Step 11~14: 사용자 API~~ | ✅ 완료 |
| ~~P0~~ | ~~Step 15: 어드민 API~~ | ✅ 완료 |
| ~~P0~~ | ~~Step 16: 동시성 테스트~~ | ✅ 완료 |

### Phase 2: 백엔드 배포 (다음 작업)

| 우선순위 | 작업 | 상태 |
|---|---|---|
| **P1** | GitHub Actions CI/CD 구성 | 대기 중 |
| **P1** | Neon PostgreSQL 프로비저닝 | 대기 중 |
| **P1** | Render/Railway 배포 | 대기 중 |
| **P1** | 환경변수 설정 (배포) | 대기 중 |
| **P1** | 배포 후 API 테스트 | 대기 중 |

### Phase 3~6: 프론트엔드/모바일

| 우선순위 | 작업 | 상태 |
|---|---|---|
| P2 | 프론트엔드(사용자) 구현 | React + TypeScript + Vite + TanStack Query |
| P2 | 어드민 웹 구현 | 사용자 웹과 별도 앱 |
| P3 | 프론트엔드/어드민 배포 | Vercel |
| P4 | 모바일(Flutter WebView) | 사용자 웹 래핑 |

---

## 3. 백엔드 API 완성 현황

**✅ 전체 API: 22/22 완료 (100%)**

### 인증 API (1개)
- ✅ POST /api/auth/login

### 사용자 API (9개)
- ✅ POST /api/user/roulette/spin
- ✅ GET /api/user/roulette/status
- ✅ GET /api/user/points (포인트 목록 - expired/expiringSoon 구분)
- ✅ GET /api/user/points/balance
- ✅ GET /api/user/points/expiring
- ✅ GET /api/user/points/history (별칭)
- ✅ GET /api/user/products
- ✅ POST /api/user/orders
- ✅ GET /api/user/orders

### 어드민 API (12개)
- ✅ GET /api/admin/dashboard
- ✅ GET /api/admin/budget
- ✅ PUT /api/admin/budget
- ✅ GET /api/admin/products (모든 상품 - ACTIVE/INACTIVE 포함)
- ✅ POST /api/admin/products
- ✅ PUT /api/admin/products/{id}
- ✅ DELETE /api/admin/products/{id}
- ✅ GET /api/admin/orders
- ✅ POST /api/admin/orders/{id}/cancel
- ✅ GET /api/admin/roulette/history
- ✅ POST /api/admin/roulette/{id}/cancel

---

## 4. 동시성 테스트 결과

**전체 7개 테스트 모두 통과 ✅**

| 테스트 | 시나리오 | 결과 |
|---|---|---|
| T-1 | 동일 유저 10개 스레드 동시 참여 | 1건만 성공, 9건 실패 ✓ |
| T-2 | 100명 동시 참여 (예산 100,000p) | 총 지급액 ≤ 100,000p ✓ |
| T-3 | 예산 500p 남은 상태 10명 동시 참여 | 최대 5명만 성공 ✓ |
| T-4 | 재고 1개 상품 3명 동시 주문 | 1건만 성공 ✓ |
| T-5 | 만료 포인트로 주문 시도 | INSUFFICIENT_POINTS 예외 ✓ |
| T-6 | 주문 취소 시 포인트 복원 | balance 정확히 복원 ✓ |
| T-7 | 룰렛 취소 시 부분 회수 | 남은 포인트만 회수 ✓ |

**테스트 파일:**
- `backend/src/test/kotlin/com/roulette/ConcurrencyTest.kt`
- `backend/src/test/resources/application-test.yml` (H2 인메모리 DB)

---

## 5. 핵심 정책 (PDP) 구현 상태

| # | 정책 | 구현 | 검증 |
|---|---|---|---|
| PDP-1 | Lazy 예산 리셋 | ✅ | getOrCreateTodayBudget() |
| PDP-2 | 동적 만료 필터 | ✅ | expires_at > NOW() 쿼리 |
| PDP-3 | FIFO 포인트 차감 | ✅ | ORDER BY expires_at ASC + SELECT FOR UPDATE |
| PDP-4 | 원래 포인트 복원 | ✅ | OrderPointUsage 기반 balance 복원 |
| PDP-5 | 당일만 예산 복구 | ✅ | KST 날짜 비교 후 조건부 복구 |
| PDP-6 | 예산 조건부 차감 | ✅ | WHERE remaining >= :amount |
| PDP-7 | 닉네임 = 유저 ID | ✅ | 자동 생성/로그인 |
| PDP-8 | 역할 완전 분리 | ✅ | SecurityConfig 필터 체인 |

---

## 6. 동시성 메커니즘 구현

### 데이터베이스 레벨
- **UNIQUE 제약**: `(user_id, spin_date)` 중복 방지
- **조건부 UPDATE**: `WHERE remaining >= :amount` 원자적 차감
- **SELECT FOR UPDATE**: 포인트 차감 시 행 잠금

### 애플리케이션 레벨
- **단일 트랜잭션**: 각 비즈니스 로직이 하나의 트랜잭션으로 완료
- **명시적 중복 체크**: `existsByUserIdAndSpinDate` + UNIQUE 제약
- **flush()**: `entityManager.flush()`로 UNIQUE 제약 즉시 확인

---

## 7. 미해결 이슈 / 보류 사항

**보류: Codex MCP 코드 리뷰**
- 상태: Codex CLI MCP 응답 없음 (Node.js 경고만 출력)
- 시도: 3회 재시도 (모두 실패)
- 리뷰 대상: 룰렛 동시성 검증 (existsByUserIdAndSpinDate + UNIQUE 제약 race condition)
- 다음 조치: MCP 재연결 후 재시도 또는 수동 리뷰

**기타 이슈:**
- 없음

---

## 8. 현재 막힌 지점

**막힌 지점 없음.**

백엔드 코어 기능이 모두 완료되어 다음 단계(배포) 진행 가능.

---

## 9. 다음 세션 첫 액션

### Option 1: 백엔드 배포 (권장)
1. GitHub Actions CI/CD 설정
   - `.github/workflows/backend-ci.yml` 생성
   - 빌드/테스트 자동화
2. Neon PostgreSQL 프로비저닝
   - 무료 티어 설정
   - 연결 문자열 발급
3. Render/Railway 배포
   - 환경변수 설정
   - PostgreSQL 연결
4. 배포 후 API 테스트
   - Swagger UI 확인
   - 주요 엔드포인트 수동 테스트

### Option 2: 프론트엔드 구현
- React + TypeScript + Vite 프로젝트 초기화
- 사용자 웹 화면 구현 (룰렛, 포인트, 상품, 주문)

### Option 3: Codex MCP 코드 리뷰 재시도
- MCP 재연결 후 동시성 검증 리뷰

---

## 10. 기술 스택 (SSOT)

| 항목 | 버전/기술 | 비고 |
|---|---|---|
| Spring Boot | **3.5.0** | SSOT 항목, 임의 변경 금지 |
| Kotlin | **2.0.21** | SSOT 항목 |
| Java | **21 LTS** | SSOT 항목 |
| Gradle | **8.12** (Kotlin DSL) | Wrapper로 관리 |
| PostgreSQL | **16** | 로컬: Docker, 배포: Neon |
| H2 | **인메모리** | 테스트 전용 |
| Swagger UI | `/swagger-ui/index.html` | SpringDoc OpenAPI 3 |
| 인증 방식 | **HTTP 세션 기반** | JWT 아님 |

---

## 11. 주요 파일 구조

```
backend/
├── src/main/kotlin/com/roulette/
│   ├── RouletteApplication.kt
│   ├── config/
│   │   ├── SecurityConfig.kt
│   │   └── AppProperties.kt
│   ├── auth/
│   │   ├── AuthController.kt
│   │   └── AuthService.kt
│   ├── common/
│   │   ├── ApiResponse.kt
│   │   ├── BusinessException.kt
│   │   └── GlobalExceptionHandler.kt
│   ├── domain/
│   │   ├── user/
│   │   ├── budget/
│   │   ├── roulette/
│   │   ├── point/
│   │   ├── product/
│   │   ├── order/
│   │   └── admin/
│   └── ...
├── src/test/kotlin/com/roulette/
│   └── ConcurrencyTest.kt (7개 시나리오)
└── build.gradle.kts
```

---

## 12. 워크플로우 규칙

- **단계별 확인 필수**: 각 단계 완료 후 요약 제시 → 사용자 확인 → 다음 단계
- **SSOT 원칙**: `CLAUDE.md` > `docs/SPEC.md` 우선순위
- **대화 로그**: `docs/PROMPT.md`에 원문 append, LAST_LOG_CURSOR로 중복 방지

---

## 13. 참조 문서

| 문서 | 경로 | 용도 |
|---|---|---|
| 프로젝트 규칙 | `CLAUDE.md` | 전체 정책/규칙/컨벤션 (SSOT) |
| 확정 명세서 | `docs/SPEC.md` | 엔티티/API/화면/테스트 상세 |
| 대화 로그 | `docs/PROMPT.md` | 원문 프롬프트/응답 기록 |
| 인계 문서 | `docs/HANDOFF.md` | 세션 간 작업 인계 |

---

## 14. 진행률

**Phase 1 백엔드 코어: 100% 완료 (6/6 단계) ✅**

- ✅ Step 11: 룰렛 기능
- ✅ Step 12: 포인트 기능
- ✅ Step 13: 상품 기능
- ✅ Step 14: 주문 기능
- ✅ Step 15: 어드민 API
- ✅ Step 16: 동시성 테스트

**전체 프로젝트: ~30% 완료**
- ✅ 백엔드 코어 (100%)
- ⬜ 백엔드 배포 (0%)
- ⬜ 프론트엔드 (0%)
- ⬜ 모바일 (0%)

---

## 15. 빠른 명령어

```bash
# 로컬 DB 시작
docker compose up -d

# 백엔드 빌드
cd backend && ./gradlew build

# 테스트 실행
./gradlew test

# 동시성 테스트만
./gradlew test --tests "com.roulette.ConcurrencyTest"

# 서버 실행
./gradlew bootRun

# Swagger UI
http://localhost:8080/swagger-ui/index.html
```
