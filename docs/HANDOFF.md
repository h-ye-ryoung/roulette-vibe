# HANDOFF.md — 세션 인계 문서

> **최종 갱신**: 2026-02-06T11:46:00+09:00

---

## 1. 완료된 작업

| # | 작업 | 상태 | 산출물 |
|---|---|---|---|
| 1 | 요구사항 분석 및 PDP 1~8 확정 | 완료 | `CLAUDE.md` §3 |
| 2 | `docs/SPEC.md` 작성 (v1.0 확정) | 완료 | 7개 엔티티, 18개 API, 12개 에러코드, 7개 동시성 테스트 시나리오 |
| 3 | `CLAUDE.md` 전면 업데이트 | 완료 | 기술 스택 SSOT 지정 (Spring Boot 3.5.0, Kotlin 2.0.21), 단계별 확인 규칙 추가 |
| 4 | Gradle 프로젝트 초기화 | 완료 | `backend/build.gradle.kts`, Gradle Wrapper 8.12 |
| 5 | Docker Compose 작성 | 완료 | `compose.yml` — PostgreSQL 16 컨테이너 |
| 6 | 설정 파일 작성 | 완료 | `application.yml`, `application-local.yml`, `application-prod.yml` |
| 7 | 환경변수 예제 작성 | 완료 | `backend/.env.example` |
| 8 | 메인 애플리케이션 작성 | 완료 | `RouletteApplication.kt` |
| 9 | Entity + JPA 매핑 (7개) | 완료 | User, DailyBudget, RouletteHistory, PointLedger, Product, Order, OrderPointUsage |
| 10 | Repository 인터페이스 + 동시성 쿼리 | 완료 | `decrementRemaining`, `decrementStock`, `findAvailableByUserIdForUpdate` |
| 11 | 세션 기반 Auth 구현 | 완료 | AuthController, AuthService, CustomUserDetailsService, SecurityConfig, SessionAuthenticationFilter |
| 12 | 예외 처리 체계 구축 | 완료 | BusinessException 계층, GlobalExceptionHandler, ApiResponse 래퍼 |
| 13 | **룰렛 기능 구현 (Step 11)** | ✅ 완료 | RouletteService, RouletteController, DTO, 중복 방지 로직 |
| 14 | 빌드 및 로컬 테스트 | 완료 | `./gradlew build` 성공, 서버 기동 성공, 룰렛 API 테스트 완료 |
| 15 | 대화 로그 기록 | 완료 | `docs/PROMPT.md` (커서: `2026-02-06T11:45:00+09:00`) |

---

## 2. 남은 작업 (우선순위순)

### Phase 1: 백엔드 코어 구현 (진행 중)

| 우선순위 | 작업 | 상태 |
|---|---|---|
| ~~P0~~ | ~~Step 11: 룰렛 기능 구현~~ | ✅ 완료 |
| **P0** | Step 12: 포인트 기능 구현 | 대기 중 (다음 작업) |
| **P0** | Step 13: 상품 기능 구현 | 대기 중 |
| **P0** | Step 14: 주문 기능 구현 (사용자: 생성/조회만, 취소 불가) | 대기 중 |
| **P0** | Step 15: 어드민 API 구현 (대시보드, 예산, 상품CRUD+DELETE, 주문/룰렛 취소) | 대기 중 |
| **P0** | Step 16: 동시성 테스트 (T-1~T-7) | 대기 중 |
| P1 | Phase 2: 백엔드 배포 | Render/Railway + Neon + GitHub Actions CI/CD |
| P2 | Phase 3: 프론트엔드(사용자) 구현 | React + TypeScript + Vite + TanStack Query + Tailwind |
| P2 | Phase 4: 어드민 웹 구현 | 사용자 웹과 별도 앱 |
| P3 | Phase 5: 프론트엔드/어드민 배포 | Vercel |
| P4 | Phase 6: 모바일(Flutter WebView) 구현 및 배포 | 사용자 웹 래핑 |

---

## 3. 확정된 설계 결정 요약

### 기술 스택 (SSOT)

| 항목 | 버전/기술 | 비고 |
|---|---|---|
| Spring Boot | **3.5.0** | SSOT 항목, 임의 변경 금지 |
| Kotlin | **2.0.21** | SSOT 항목 |
| Java | **21 LTS** | SSOT 항목 |
| Gradle | **8.12** (Kotlin DSL) | Wrapper로 관리 |
| PostgreSQL | **16** | 로컬: Docker, 배포: Neon |
| Swagger UI | `/swagger-ui/index.html` | SpringDoc OpenAPI 3 |
| 인증 방식 | **HTTP 세션 기반** | JWT 아님, 닉네임 mocking 로그인 |

### 핵심 정책 (PDP)

| # | 정책 | 구현 방법 |
|---|---|---|
| PDP-1 | Lazy 예산 리셋 | budget_date vs 오늘(KST) 비교, 다르면 새 행 생성 |
| PDP-2 | 동적 만료 필터 | `expires_at > NOW()` 쿼리 조건 |
| PDP-3 | FIFO 포인트 차감 | `ORDER BY expires_at ASC` + `SELECT FOR UPDATE` |
| PDP-4 | 원래 포인트 복원 | balance 되돌림, 만료 상태는 사용 불가 |
| PDP-5 | 당일만 예산 복구 | 취소일(KST) = 지급일이면 복구 |
| PDP-6 | 예산 조건부 차감 | `UPDATE ... WHERE remaining >= :amount` |
| PDP-7 | 닉네임 = 유저 ID | 자동 생성/로그인, 회원가입 없음 |
| PDP-8 | 역할 완전 분리 | USER ↔ ADMIN 상호 403, `/api/auth/**` 공개 |

### 동시성 전략

- **중복 참여 방지**:
  - 1차 방어: `existsByUserIdAndSpinDate` 명시적 체크
  - 2차 방어: `UNIQUE(user_id, spin_date)` + `entityManager.flush()`
- **예산 원자적 차감**: `UPDATE daily_budget SET remaining = remaining - :amount WHERE remaining >= :amount`
- **재고 원자적 차감**: `UPDATE product SET stock = stock - 1 WHERE id = :id AND stock > 0`
- **포인트 FIFO 차감**: `SELECT ... FOR UPDATE` + balance 차감
- **JPA 쿼리 최적화**: `@Modifying(clearAutomatically = true, flushAutomatically = true)`

---

## 4. Step 11 (룰렛 기능) 구현 상세

### 구현 파일

```
backend/src/main/kotlin/com/roulette/domain/roulette/
├── dto/
│   ├── SpinResponse.kt
│   └── RouletteStatusResponse.kt
├── RouletteService.kt
└── RouletteController.kt
```

### 핵심 로직

**RouletteService.spin(userId)**
1. 중복 참여 체크: `existsByUserIdAndSpinDate` (명시적 검증)
2. 랜덤 금액 생성: `Random.nextInt(100, 1001)`
3. Lazy 예산 리셋: `getOrCreateTodayBudget(today)`
4. 예산 원자적 차감: `decrementRemaining(today, amount)` → 0이면 `BUDGET_EXHAUSTED`
5. RouletteHistory 저장 + `entityManager.flush()` → UNIQUE 위반 시 `ALREADY_PARTICIPATED`
6. PointLedger 저장: `expiresAt = now + 30일`
7. 잔여 예산 조회 후 응답

**RouletteService.getStatus(userId)**
- 오늘 참여 이력 조회
- 오늘 예산 조회 (없으면 기본값 반환)

### 동시성 처리

- **1차 방어**: 트랜잭션 시작 시 DB 조회로 중복 체크
- **2차 방어**: UNIQUE 제약 + flush로 동시 INSERT 차단
- **예산 차감**: 조건부 UPDATE로 원자적 처리

### 테스트 결과

✅ **로그인** — 성공
✅ **Status 조회** — 성공 (participated: false, remainingBudget: 100000)
✅ **첫 번째 Spin** — 성공 (200p 당첨, remainingBudget: 98251)
✅ **중복 Spin** — 에러 응답 정상 반환
```json
{
  "success": false,
  "error": {
    "code": "ALREADY_PARTICIPATED",
    "message": "You have already participated in today's roulette"
  }
}
```

✅ **DB 중복 차단** — 확인 (user_id, spin_date별 1건만 존재)

---

## 5. 미해결 이슈 / 보류 사항

**없음.** 룰렛 기능 완전히 구현 및 테스트 완료.

---

## 6. 현재 막힌 지점

**막힌 지점 없음.** 다음 단계(포인트 기능) 진행 가능.

---

## 7. 다음 세션 첫 3개 액션

### Action 1: 포인트 기능 구현 (PointService, PointController)

**구현 내용:**
- `PointService.getBalance(userId: Long)`
  - `expires_at > NOW()` 조건으로 유효 포인트만 합산
  - 만료 임박 포인트 목록 (7일 이내)
  - 응답: `{ totalBalance: Int, expiringPoints: List<{amount, expiresAt}> }`

- `PointService.getHistory(userId: Long, page: Int, size: Int)`
  - PointLedger 전체 내역 조회 (페이지네이션)
  - 응답: `{ items: [...], totalCount, currentPage, totalPages }`

**구현 파일:**
- `backend/src/main/kotlin/com/roulette/domain/point/PointService.kt`
- `backend/src/main/kotlin/com/roulette/domain/point/PointController.kt`
- `backend/src/main/kotlin/com/roulette/domain/point/dto/BalanceResponse.kt`
- `backend/src/main/kotlin/com/roulette/domain/point/dto/PointHistoryResponse.kt`

**API:**
- `GET /api/user/points/balance` → `{ totalBalance, expiringPoints: [...] }`
- `GET /api/user/points/history?page=0&size=20` → `{ items: [...], totalCount, ... }`

**검증:**
- 단위 테스트: 만료 포인트 제외 확인
- 통합 테스트: 실제 DB 조회 결과 검증
- API 테스트: curl로 잔액 조회 확인

---

### Action 2: 상품 기능 구현 (ProductService, ProductController)

**구현 내용:**
- `ProductService.getProducts(isActive: Boolean = true)`
  - `isActive` 필터 적용
  - 활성화된 상품만 반환 (기본값)

- `ProductService.getProduct(id: Long)`
  - 상품 상세 정보 조회
  - 존재하지 않으면 `PRODUCT_NOT_FOUND`

**구현 파일:**
- `backend/src/main/kotlin/com/roulette/domain/product/ProductService.kt`
- `backend/src/main/kotlin/com/roulette/domain/product/ProductController.kt`
- `backend/src/main/kotlin/com/roulette/domain/product/dto/ProductListResponse.kt`
- `backend/src/main/kotlin/com/roulette/domain/product/dto/ProductDetailResponse.kt`

**API:**
- `GET /api/user/products` → `{ items: [...] }`
- `GET /api/user/products/{id}` → `{ id, name, price, stock, ... }`

**검증:**
- `isActive=true` 필터 동작 확인
- 재고 0인 상품도 목록에 표시되는지 확인

---

### Action 3: 주문 기능 구현 (OrderService, OrderController) — 가장 복잡

**구현 내용:**
- `OrderService.createOrder(userId: Long, productId: Long)`
  - **단일 트랜잭션 내:**
    1. Product 조회 + 재고 원자적 차감 (`decrementStock`)
    2. 유효 포인트 FIFO 조회 (`findAvailableByUserIdForUpdate`)
    3. 포인트 부족 체크 → `INSUFFICIENT_POINTS`
    4. FIFO 순으로 balance 차감 (N개 PointLedger UPDATE)
    5. Order INSERT
    6. OrderPointUsage INSERT (N건)

- `OrderService.cancelOrder(userId: Long, orderId: Long)`
  - **단일 트랜잭션 내:**
    1. Order 조회 + 권한 체크
    2. 이미 취소 체크 → `ORDER_ALREADY_CANCELLED`
    3. Order 상태 → `CANCELLED`
    4. OrderPointUsage 조회 → PointLedger balance 복원
    5. Product stock 복원

**구현 파일:**
- `backend/src/main/kotlin/com/roulette/domain/order/OrderService.kt`
- `backend/src/main/kotlin/com/roulette/domain/order/OrderController.kt`
- DTO: `CreateOrderRequest`, `OrderResponse`, `OrderListResponse`

**API:**
- `POST /api/user/orders` → `{ orderId, totalPrice, usedPoints }`
- `POST /api/user/orders/{id}/cancel` → `{ success, refundedPoints }`
- `GET /api/user/orders` → `{ items: [...] }`

**검증:**
- 재고 0 → `PRODUCT_OUT_OF_STOCK`
- 포인트 부족 → `INSUFFICIENT_POINTS`
- FIFO 차감 순서 확인
- 취소 후 잔액 복원 확인

---

## 8. 워크플로우 규칙

- **단계별 확인 필수**: 각 단계 완료 후 요약을 제시하고, 사용자 확인을 받은 뒤 다음 단계로 진행
- **SSOT 원칙**: `CLAUDE.md` > `docs/SPEC.md` 우선순위. 버전/정책 등 SSOT 항목은 임의 변경 금지
- **대화 로그**: `docs/PROMPT.md`에 가공 없이 원문 append, LAST_LOG_CURSOR로 중복 방지

---

## 9. 참조 문서

| 문서 | 경로 | 용도 |
|---|---|---|
| 프로젝트 규칙 | `CLAUDE.md` | 전체 정책/규칙/컨벤션 (SSOT) |
| 확정 명세서 | `docs/SPEC.md` | 엔티티/API/화면/테스트 상세 |
| 대화 로그 | `docs/PROMPT.md` | 원문 프롬프트/응답 기록 |
| 구현 계획 | `~/.claude/plans/indexed-wondering-squirrel.md` | Phase 1 상세 계획 |

---

## 10. 진행률

**Phase 1 백엔드 코어: 16.7% 완료 (1/6 단계)**

- ✅ Step 11: 룰렛 기능
- ⬜ Step 12: 포인트 기능
- ⬜ Step 13: 상품 기능
- ⬜ Step 14: 주문 기능
- ⬜ Step 15: 어드민 API
- ⬜ Step 16: 동시성 테스트

**전체 프로젝트: ~7% 완료**
