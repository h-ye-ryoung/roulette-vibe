# SPEC.md — 포인트 룰렛 서비스 명세서

> **버전**: v1.0
> **작성일**: 2026-02-05
> **상태**: 확정

---

## 1. 서비스 개요

매일 룰렛을 돌려 포인트를 획득하고, 획득한 포인트로 상품을 구매하는 서비스.

### 1.1 핵심 제약

| 제약 | 상세 |
|---|---|
| 1일 1회 참여 | 유저별 하루 한 번만 룰렛 참여 가능 |
| 일일 예산 | 하루 총 100,000p. 소진 시 당첨 불가 |
| 랜덤 포인트 | 100p ~ 1,000p 범위 |
| 포인트 유효기간 | 획득일로부터 30일. 만료 포인트 사용 불가 |
| 시간 기준 | 모든 날짜 계산은 **KST(Asia/Seoul)** 고정 |

### 1.2 앱 구조

사용자 웹(frontend)과 어드민 웹(admin)은 **완전히 분리된 앱**으로 운영한다.

- `frontend/` — 사용자 전용 (룰렛, 포인트, 상품, 주문)
- `admin/` — 어드민 전용 (예산, 상품 CRUD, 취소/환불, 대시보드)
- `mobile/` — Flutter WebView (사용자 웹 렌더링)

---

## 2. 확정 정책 (PDP)

### PDP-1: 예산 리셋 — Lazy 리셋

- `DailyBudget` 테이블에 `budget_date` 저장
- 룰렛 요청 시 오늘(KST) 날짜와 비교
- 날짜가 다르면 새 행 생성 (`remaining = daily_limit`)
- 스케줄러 불필요. 무료 배포 환경에서 서버 sleep 문제 회피

### PDP-2: 포인트 만료 — 동적 필터

- 포인트 레코드에 `expires_at` 저장
- 잔액 조회/주문 시 `expires_at > NOW()` 조건으로 유효분만 사용
- 목록 조회 시 `expires_at < NOW()`인 경우 "만료됨" 레이블 표시
- 별도 배치/스케줄러 불필요

### PDP-3: 포인트 차감 순서 — FIFO (만료 임박 순)

- 주문 시 `expires_at ASC` 순서로 유효 포인트부터 차감
- 소멸 직전 포인트를 먼저 소진하여 유저 이익 극대화

### PDP-4: 환불 포인트 유효기간 — 원래 포인트 복원

- 주문 취소 시 차감했던 포인트 레코드의 `balance`를 복원
- 이미 만료된 포인트(`expires_at < NOW()`)는 복원되어도 사용 불가
- 유저에게 "환불되었지만 해당 포인트는 이미 만료" 안내 가능

### PDP-5: 룰렛 취소 시 예산 복구 — 당일만

- 취소 당일(KST)이 지급일과 같은 날이면 `DailyBudget.remaining` 복구
- 다른 날이면 예산 복구하지 않음 (과거 예산은 닫힌 것으로 간주)

### PDP-6: 예산 부족 시 처리

- 룰렛 참여 시 먼저 랜덤 금액(100~1,000p)을 결정
- `UPDATE daily_budget SET remaining = remaining - :amount WHERE remaining >= :amount` 시도
- 실패(영향 행 0) 시 "예산 소진" 응답
- 잔여가 100p 미만이면 사실상 소진 상태

### PDP-7: 유저 식별 — 닉네임 = 유저 ID

- 닉네임 입력 시 해당 닉네임의 유저가 없으면 자동 생성, 있으면 로그인
- 별도 회원가입 절차 없음
- 닉네임에 UNIQUE 제약 적용

---

## 3. 도메인 모델

### 3.1 엔티티 정의

#### User

| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | Long (PK) | AUTO_INCREMENT | |
| nickname | String | UNIQUE, NOT NULL | 로그인 식별자 |
| created_at | Timestamp | NOT NULL | |

#### DailyBudget

| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | Long (PK) | AUTO_INCREMENT | |
| budget_date | LocalDate | UNIQUE, NOT NULL | KST 기준 날짜 |
| daily_limit | Int | NOT NULL, DEFAULT 100000 | 일일 총 예산 |
| remaining | Int | NOT NULL | 잔여 예산 (원자적 차감 대상) |
| updated_at | Timestamp | NOT NULL | |

#### RouletteHistory

| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | Long (PK) | AUTO_INCREMENT | |
| user_id | Long (FK → User) | NOT NULL | |
| spin_date | LocalDate | NOT NULL | KST 기준 참여 날짜 |
| amount | Int | NOT NULL | 당첨 금액 (100~1000) |
| status | Enum | NOT NULL, DEFAULT ACTIVE | ACTIVE / CANCELLED |
| created_at | Timestamp | NOT NULL | |
| | | **UNIQUE(user_id, spin_date)** | 1일 1회 보장 |

#### PointLedger

| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | Long (PK) | AUTO_INCREMENT | |
| user_id | Long (FK → User) | NOT NULL | |
| roulette_history_id | Long (FK → RouletteHistory) | NULLABLE | 룰렛 지급 시 연결 |
| amount | Int | NOT NULL | 원래 지급 금액 |
| balance | Int | NOT NULL | 잔여 사용 가능 금액 |
| type | Enum | NOT NULL | EARN / REFUND |
| issued_at | Timestamp | NOT NULL | 지급 시점 |
| expires_at | Timestamp | NOT NULL | issued_at + 30일 |
| created_at | Timestamp | NOT NULL | |

#### Product

| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | Long (PK) | AUTO_INCREMENT | |
| name | String | NOT NULL | |
| description | String | NULLABLE | |
| price | Int | NOT NULL, > 0 | 포인트 가격 |
| stock | Int | NOT NULL, >= 0 | 재고 수량 |
| status | Enum | NOT NULL, DEFAULT ACTIVE | ACTIVE / INACTIVE |
| created_at | Timestamp | NOT NULL | |
| updated_at | Timestamp | NOT NULL | |

#### Order

| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | Long (PK) | AUTO_INCREMENT | |
| user_id | Long (FK → User) | NOT NULL | |
| product_id | Long (FK → Product) | NOT NULL | |
| total_price | Int | NOT NULL | 결제 포인트 총액 |
| status | Enum | NOT NULL, DEFAULT COMPLETED | COMPLETED / CANCELLED |
| created_at | Timestamp | NOT NULL | |
| cancelled_at | Timestamp | NULLABLE | 취소 시점 |

#### OrderPointUsage

| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | Long (PK) | AUTO_INCREMENT | |
| order_id | Long (FK → Order) | NOT NULL | |
| point_ledger_id | Long (FK → PointLedger) | NOT NULL | |
| amount | Int | NOT NULL | 해당 포인트에서 차감한 금액 |

### 3.2 엔티티 관계

```
User ──1:N──> RouletteHistory
User ──1:N──> PointLedger
User ──1:N──> Order
Order ──1:N──> OrderPointUsage ──N:1──> PointLedger
RouletteHistory ──1:1──> PointLedger (EARN 지급 시)
Product ──1:N──> Order
```

---

## 4. API 명세

### 4.1 공통

#### 기본 경로

- 인증: `/api/auth/**`
- 사용자: `/api/user/**`
- 어드민: `/api/admin/**`

#### 성공 응답

```json
{
  "success": true,
  "data": { ... }
}
```

#### 실패 응답

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "사용자 친화적 메시지"
  }
}
```

#### 에러 코드 목록

| 코드 | HTTP | 설명 |
|---|---|---|
| `ALREADY_PARTICIPATED` | 409 | 오늘 이미 참여 |
| `BUDGET_EXHAUSTED` | 409 | 예산 소진 |
| `INSUFFICIENT_POINTS` | 400 | 포인트 부족 (만료 포인트만 보유 포함) |
| `PRODUCT_NOT_FOUND` | 404 | 상품 없음 |
| `PRODUCT_OUT_OF_STOCK` | 409 | 재고 소진 |
| `PRODUCT_HAS_ORDERS` | 409 | 주문 내역이 있어 상품 삭제 불가 |
| `ORDER_NOT_FOUND` | 404 | 주문 없음 |
| `ORDER_ALREADY_CANCELLED` | 409 | 이미 취소된 주문 |
| `ROULETTE_NOT_FOUND` | 404 | 룰렛 이력 없음 |
| `ROULETTE_ALREADY_CANCELLED` | 409 | 이미 취소된 룰렛 |
| `FORBIDDEN` | 403 | 권한 없음 (role 불일치) |
| `UNAUTHORIZED` | 401 | 인증 필요 |
| `USER_NOT_FOUND` | 404 | 유저 없음 |

---

### 4.2 인증 API (`/api/auth`)

#### POST `/api/auth/login`

닉네임으로 로그인. 유저가 없으면 자동 생성.

**Request**
```json
{
  "nickname": "string (1~20자)"
}
```

**Response 200**
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "nickname": "player1",
    "role": "USER",
    "token": "세션 토큰 또는 JWT"
  }
}
```

---

### 4.3 사용자 API (`/api/user`)


#### POST `/api/user/roulette/spin`

룰렛 참여. 1일 1회, 100~1,000p 랜덤 지급.

**Request**: 없음 (인증 토큰에서 유저 식별)

**Response 200**
```json
{
  "success": true,
  "data": {
    "historyId": 1,
    "amount": 350,
    "remainingBudget": 99650,
    "message": "350p 당첨!"
  }
}
```

**에러**: `ALREADY_PARTICIPATED`, `BUDGET_EXHAUSTED`

---

#### GET `/api/user/roulette/status`

오늘 참여 여부 + 잔여 예산.

**Response 200**
```json
{
  "success": true,
  "data": {
    "participated": true,
    "todayAmount": 350,
    "remainingBudget": 99650
  }
}
```

---

#### GET `/api/user/points`

내 포인트 목록. 유효/만료 구분.

**Query Params**: `page`, `size` (페이지네이션)

**Response 200**
```json
{
  "success": true,
  "data": {
    "points": [
      {
        "id": 1,
        "amount": 350,
        "balance": 200,
        "type": "EARN",
        "issuedAt": "2026-02-05T10:00:00+09:00",
        "expiresAt": "2026-03-07T10:00:00+09:00",
        "expired": false
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1
  }
}
```

---

#### GET `/api/user/points/balance`

사용 가능 잔액 합계 (유효 포인트만).

**Response 200**
```json
{
  "success": true,
  "data": {
    "balance": 1200
  }
}
```

---

#### GET `/api/user/points/expiring`

7일 내 만료 예정 포인트.

**Response 200**
```json
{
  "success": true,
  "data": {
    "expiringPoints": [
      {
        "id": 3,
        "balance": 150,
        "expiresAt": "2026-02-10T10:00:00+09:00"
      }
    ],
    "totalExpiringBalance": 150
  }
}
```

---

#### GET `/api/user/products`

구매 가능 상품 목록 (status = ACTIVE, stock > 0).

**Response 200**
```json
{
  "success": true,
  "data": {
    "products": [
      {
        "id": 1,
        "name": "커피 쿠폰",
        "description": "아메리카노 1잔",
        "price": 500,
        "stock": 10
      }
    ]
  }
}
```

---

#### POST `/api/user/orders`

상품 주문. 포인트 FIFO 차감.

**Request**
```json
{
  "productId": 1
}
```

**Response 200**
```json
{
  "success": true,
  "data": {
    "orderId": 1,
    "productName": "커피 쿠폰",
    "totalPrice": 500,
    "remainingBalance": 700
  }
}
```

**에러**: `INSUFFICIENT_POINTS`, `PRODUCT_OUT_OF_STOCK`, `PRODUCT_NOT_FOUND`

---

#### GET `/api/user/orders`

내 주문 내역.

**Query Params**: `page`, `size`

**Response 200**
```json
{
  "success": true,
  "data": {
    "orders": [
      {
        "id": 1,
        "productName": "커피 쿠폰",
        "totalPrice": 500,
        "status": "COMPLETED",
        "createdAt": "2026-02-05T11:00:00+09:00",
        "cancelledAt": null
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1
  }
}
```

---

### 4.4 어드민 API (`/api/admin`)

#### GET `/api/admin/dashboard`

오늘 예산 현황, 참여자 수, 지급 총액.

**Response 200**
```json
{
  "success": true,
  "data": {
    "budgetDate": "2026-02-05",
    "dailyLimit": 100000,
    "remaining": 85000,
    "usedAmount": 15000,
    "participantCount": 25
  }
}
```

---

#### GET `/api/admin/budget`

오늘 예산 상세 조회.

**Response 200**
```json
{
  "success": true,
  "data": {
    "budgetDate": "2026-02-05",
    "dailyLimit": 100000,
    "remaining": 85000
  }
}
```

---

#### PUT `/api/admin/budget`

일일 예산 설정. **다음 날부터 적용**.

**Request**
```json
{
  "dailyLimit": 150000
}
```

**Response 200**
```json
{
  "success": true,
  "data": {
    "dailyLimit": 150000,
    "effectiveFrom": "2026-02-06"
  }
}
```

---

#### GET `/api/admin/products`

전체 상품 목록 (ACTIVE + INACTIVE 포함).

---

#### POST `/api/admin/products`

상품 등록.

**Request**
```json
{
  "name": "커피 쿠폰",
  "description": "아메리카노 1잔",
  "price": 500,
  "stock": 100
}
```

---

#### PUT `/api/admin/products/{id}`

상품 수정.

**Request**
```json
{
  "name": "커피 쿠폰 (수정)",
  "description": "라떼 1잔",
  "price": 600,
  "stock": 50,
  "status": "ACTIVE"
}
```

---

#### DELETE `/api/admin/products/{id}`

상품 삭제. 물리적 삭제(hard delete)로 데이터베이스에서 완전히 제거.

**사전 조건**: 해당 상품으로 생성된 주문이 없어야 함 (주문이 있으면 삭제 불가).

**Response 200**
```json
{
  "success": true,
  "data": {
    "message": "상품이 삭제되었습니다"
  }
}
```

**에러**:
- `PRODUCT_NOT_FOUND` (404) — 상품 없음
- `PRODUCT_HAS_ORDERS` (409) — 주문 내역이 있어 삭제 불가

---

#### GET `/api/admin/orders`

전체 주문 내역.

**Query Params**: `page`, `size`, `status` (필터)

---

#### POST `/api/admin/orders/{id}/cancel`

특정 사용자의 주문을 어드민이 취소하고 포인트를 환불합니다.

**동작**:
1. 주문 상태를 `CANCELLED`로 변경
2. OrderPointUsage 조회 후 각 PointLedger의 balance 복원
3. Product의 stock 복원
4. 환불된 포인트 중 이미 만료된 것은 복원되어도 사용 불가

**Request**: 없음 (URL 파라미터로 orderId만 전달)

**Response 200**
```json
{
  "success": true,
  "data": {
    "orderId": 1,
    "userId": 5,
    "userName": "player1",
    "refundedAmount": 500,
    "message": "주문이 취소되고 포인트가 환불되었습니다"
  }
}
```

**에러**: `ORDER_NOT_FOUND`, `ORDER_ALREADY_CANCELLED`

---

#### GET `/api/admin/roulette/history`

룰렛 참여 내역.

**Query Params**: `page`, `size`, `date` (날짜 필터)

---

#### POST `/api/admin/roulette/{id}/cancel`

특정 사용자의 룰렛 참여를 어드민이 취소하고 포인트를 회수합니다.

**동작**:
1. RouletteHistory 상태를 `CANCELLED`로 변경
2. 해당 룰렛으로 지급된 PointLedger의 balance를 조회
3. **이미 사용된 포인트(balance < amount)는 회수하지 않음**
4. 남은 포인트만 회수: balance를 0으로 설정
5. 당일(KST) 취소인 경우, DailyBudget의 remaining 복구

**사전 조건**: 없음 (부분 사용 중이어도 취소 가능, 남은 포인트만 회수)

**Response 200**
```json
{
  "success": true,
  "data": {
    "historyId": 1,
    "userId": 5,
    "userName": "player1",
    "originalAmount": 350,
    "reclaimedAmount": 150,
    "alreadyUsedAmount": 200,
    "budgetRestored": true,
    "message": "룰렛이 취소되었습니다. 사용하지 않은 150p를 회수했습니다."
  }
}
```

**에러**: `ROULETTE_NOT_FOUND`, `ROULETTE_ALREADY_CANCELLED`

> `budgetRestored`: 당일 취소면 true (예산 복구), 다른 날이면 false.
> 이미 사용된 포인트는 회수하지 않으므로, 부분 사용 중이어도 취소 가능.
