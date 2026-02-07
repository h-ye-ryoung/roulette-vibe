# ADMIN_SPEC.md — 어드민 웹 프론트엔드 명세서

> **버전**: v1.0
> **작성일**: 2026-02-07
> **상태**: 확정
> **관련 문서**: `docs/SPEC.md` (백엔드 API 명세)

---

## 1. 개요

백엔드 API가 완성되고 배포된 상태에서, 어드민 관리자용 웹 애플리케이션을 구축합니다. 사용자 웹과 별도로 분리된 어드민 전용 웹으로, 룰렛 예산 관리, 상품 관리, 주문 관리, 룰렛 내역 관리 기능을 제공합니다.

### 1.1 목표

- [x] 어드민 전용 웹 애플리케이션 구축 (사용자 웹과 완전 분리)
- [x] 6개 페이지 구현 (로그인, 대시보드, 예산, 룰렛, 상품, 주문)
- [x] shadcn/ui 기반 깔끔한 UI/UX
- [x] 백엔드 API 22개 중 어드민 API 13개 연동
- [x] TanStack Query로 효율적인 서버 상태 관리
- [x] React Hook Form + Zod로 폼 검증

### 1.2 비목표 (Not Goals)

- ❌ 사용자 웹 기능 (별도 프로젝트)
- ❌ 모바일 반응형 (데스크톱 우선, 태블릿/모바일은 선택사항)
- ❌ 실시간 데이터 (WebSocket, SSE 등)
- ❌ 복잡한 차트/그래프 (기본 프로그레스 바만)

---

## 2. 기술 스택

| 항목 | 기술 | 버전 | 비고 |
|---|---|---|---|
| **프레임워크** | React | 18+ | Hooks 기반 |
| **언어** | TypeScript | 5+ | strict 모드 |
| **빌드 도구** | Vite | 5+ | 빠른 개발 서버 |
| **라우팅** | React Router | v6 | 선언적 라우팅 |
| **API 호출** | Axios | 1.6+ | 세션 쿠키 지원 |
| **서버 상태 관리** | TanStack Query | v5 | React Query |
| **폼 처리** | React Hook Form | 7+ | 비제어 컴포넌트 |
| **폼 검증** | Zod | 3+ | 타입 안전 스키마 |
| **스타일링** | Tailwind CSS | 3+ | 유틸리티 우선 |
| **UI 컴포넌트** | shadcn/ui | latest | Radix UI 기반 |
| **클라이언트 상태** | React Context | - | 인증 상태만 |

---

## 3. 화면 구성 (6개 페이지)

### 3.1 로그인 페이지 (`/login`)

**URL**: `/login`
**인증**: 불필요
**API**: `POST /api/auth/login`

#### 화면 구성
- 중앙 정렬 로그인 카드
- 닉네임 입력 필드 (1~50자)
- 로그인 버튼
- 에러 메시지 표시 영역

#### 기능 요구사항
1. 닉네임 입력 후 로그인 버튼 클릭
2. `POST /api/auth/login` 호출
3. 응답에서 `role` 확인:
   - `role === "ADMIN"` → 대시보드(`/`)로 리다이렉트
   - `role !== "ADMIN"` → "어드민 권한이 없습니다" 에러 표시
4. 세션 쿠키 자동 저장 (withCredentials: true)
5. 이미 로그인 상태면 대시보드로 자동 리다이렉트

#### 검증 규칙 (Zod)
```typescript
const loginSchema = z.object({
  nickname: z.string()
    .min(1, '닉네임을 입력하세요')
    .max(50, '닉네임은 50자 이하여야 합니다'),
});
```

---

### 3.2 대시보드 (`/`)

**URL**: `/`
**인증**: 필수 (ADMIN)
**API**: `GET /api/admin/dashboard`

#### 화면 구성
- 3개의 통계 카드 (Card 컴포넌트)
  1. **일일 예산**: dailyLimit 표시 (예: "100,000p")
  2. **남은 예산**: remaining 표시 + 소진율 프로그레스 바
  3. **참여자 수**: participantCount 표시
- 소진율 계산: `(usedAmount / dailyLimit) * 100`

#### 기능 요구사항
1. 페이지 로드 시 `GET /api/admin/dashboard` 호출
2. 로딩 중: Skeleton 표시
3. 에러 시: 에러 메시지 표시 + 재시도 버튼
4. 성공 시: 카드 3개 표시
5. 자동 새로고침 없음 (수동 새로고침만)

#### API 응답 예시
```json
{
  "success": true,
  "data": {
    "budgetDate": "2026-02-07",
    "dailyLimit": 100000,
    "remaining": 85000,
    "usedAmount": 15000,
    "participantCount": 25
  }
}
```

---

### 3.3 예산 관리 (`/budget`)

**URL**: `/budget`
**인증**: 필수 (ADMIN)
**API**:
- `GET /api/admin/budget` (조회)
- `PUT /api/admin/budget` (변경)

#### 화면 구성
- **현재 예산 표시 섹션**
  - 예산 날짜 (budgetDate)
  - 일일 예산 한도 (dailyLimit)
  - 남은 예산 (remaining)
- **예산 변경 폼**
  - 새 일일 예산 입력 필드 (숫자)
  - 저장 버튼
  - ⚠️ **안내 메시지**: "예산 변경은 다음 날(KST)부터 적용됩니다"

#### 기능 요구사항
1. 페이지 로드 시 `GET /api/admin/budget` 호출
2. 예산 변경 폼 제출 시:
   - `PUT /api/admin/budget` 호출
   - 성공 시: Toast("예산이 변경되었습니다")
   - 실패 시: Toast(에러 메시지)
   - 성공 후 자동으로 데이터 재조회 (invalidateQueries)

#### 검증 규칙 (Zod)
```typescript
const budgetSchema = z.object({
  dailyLimit: z.number()
    .min(1000, '예산은 1,000p 이상이어야 합니다')
    .max(10000000, '예산은 10,000,000p 이하여야 합니다'),
});
```

#### API 응답 예시
```json
// GET /api/admin/budget
{
  "success": true,
  "data": {
    "budgetDate": "2026-02-07",
    "dailyLimit": 100000,
    "remaining": 85000
  }
}

// PUT /api/admin/budget
{
  "success": true,
  "data": {
    "dailyLimit": 150000,
    "effectiveFrom": "2026-02-08"
  }
}
```

---

### 3.4 룰렛 관리 (`/roulette`)

**URL**: `/roulette`
**인증**: 필수 (ADMIN)
**API**:
- `GET /api/admin/roulette/history?page={page}&size={size}&date={date}` (내역 조회)
- `POST /api/admin/roulette/{id}/cancel` (취소)

#### 화면 구성
- **필터 영역**
  - 날짜 필터 (DatePicker, 선택 사항)
  - 필터 초기화 버튼
- **테이블** (shadcn/ui Table)
  - 컬럼: ID, 사용자명, 지급 포인트, 남은 포인트, 날짜, 상태, 액션
  - 상태: ACTIVE (초록), CANCELLED (회색)
  - 액션: 취소 버튼 (상태가 ACTIVE일 때만 표시)
- **페이지네이션**
  - 이전/다음 버튼
  - 현재 페이지/전체 페이지 표시

#### 기능 요구사항
1. 페이지 로드 시 `GET /api/admin/roulette/history?page=0&size=20` 호출
2. 날짜 필터 변경 시:
   - `GET /api/admin/roulette/history?page=0&size=20&date=YYYY-MM-DD` 호출
3. 취소 버튼 클릭 시:
   - 확인 다이얼로그: "이 룰렛 참여를 취소하시겠습니까? 남은 포인트만 회수됩니다."
   - 확인 시: `POST /api/admin/roulette/{id}/cancel` 호출
   - 성공 시: Toast(성공 메시지) + 데이터 재조회
   - 실패 시: Toast(에러 메시지)
4. 페이지네이션:
   - 페이지 변경 시 쿼리 파라미터 업데이트 (`?page={page}`)

#### 테이블 컬럼 정의
| 컬럼 | 타입 | 설명 |
|---|---|---|
| ID | number | historyId |
| 사용자명 | string | userName |
| 지급 포인트 | number | originalAmount (1,000p 형식) |
| 남은 포인트 | number | reclaimedAmount (1,000p 형식) |
| 날짜 | string | spinDate (YYYY-MM-DD) |
| 상태 | Badge | status (ACTIVE/CANCELLED) |
| 액션 | Button | 취소 버튼 (ACTIVE만) |

---

### 3.5 상품 관리 (`/products`)

**URL**: `/products`
**인증**: 필수 (ADMIN)
**API**:
- `GET /api/admin/products` (목록 조회)
- `POST /api/admin/products` (생성)
- `PUT /api/admin/products/{id}` (수정)
- `DELETE /api/admin/products/{id}` (삭제)

#### 화면 구성
- **상단 액션 영역**
  - "상품 추가" 버튼 (Dialog 열기)
- **테이블** (shadcn/ui Table)
  - 컬럼: ID, 상품명, 설명, 가격, 재고, 상태, 액션
  - 상태: ACTIVE (초록), INACTIVE (회색)
  - 재고 0: 빨간색 배지 표시
  - 액션: 수정 버튼, 삭제 버튼
- **상품 추가/수정 Dialog**
  - 상품명 입력 (1~100자)
  - 설명 입력 (1~500자, textarea)
  - 가격 입력 (숫자, 1p 이상)
  - 재고 입력 (숫자, 0 이상)
  - 상태 선택 (ACTIVE/INACTIVE, Select)
  - 저장 버튼, 취소 버튼

#### 기능 요구사항
1. 페이지 로드 시 `GET /api/admin/products` 호출
2. 상품 추가 버튼 클릭:
   - Dialog 열기
   - 폼 초기화 (빈 값)
3. 수정 버튼 클릭:
   - Dialog 열기
   - 폼에 기존 값 채우기 (defaultValues)
4. 저장 버튼 클릭:
   - 생성: `POST /api/admin/products` 호출
   - 수정: `PUT /api/admin/products/{id}` 호출
   - 성공 시: Dialog 닫기 + Toast + 데이터 재조회
   - 실패 시: Toast(에러 메시지)
5. 삭제 버튼 클릭:
   - 확인 다이얼로그: "이 상품을 삭제하시겠습니까?"
   - 확인 시: `DELETE /api/admin/products/{id}` 호출
   - 성공 시: Toast + 데이터 재조회
   - 실패 시 (주문 내역 있음): Toast("주문 내역이 있어 삭제할 수 없습니다")

#### 검증 규칙 (Zod)
```typescript
const productSchema = z.object({
  name: z.string()
    .min(1, '상품명을 입력하세요')
    .max(100, '상품명은 100자 이하여야 합니다'),
  description: z.string()
    .min(1, '설명을 입력하세요')
    .max(500, '설명은 500자 이하여야 합니다'),
  price: z.number()
    .min(1, '가격은 1p 이상이어야 합니다'),
  stock: z.number()
    .min(0, '재고는 0 이상이어야 합니다'),
  isActive: z.boolean().optional(),
});
```

---

### 3.6 주문 관리 (`/orders`)

**URL**: `/orders`
**인증**: 필수 (ADMIN)
**API**:
- `GET /api/admin/orders?page={page}&size={size}&status={status}` (목록 조회)
- `POST /api/admin/orders/{id}/cancel` (취소)

#### 화면 구성
- **필터 영역**
  - 상태 필터 (Select): 전체, COMPLETED, CANCELLED
  - 필터 초기화 버튼
- **테이블** (shadcn/ui Table)
  - 컬럼: 주문번호, 사용자명, 상품명, 금액, 상태, 주문일시, 액션
  - 상태: COMPLETED (초록), CANCELLED (회색)
  - 액션: 취소 버튼 (COMPLETED 상태만 표시)
- **페이지네이션**
  - 이전/다음 버튼
  - 현재 페이지/전체 페이지 표시

#### 기능 요구사항
1. 페이지 로드 시 `GET /api/admin/orders?page=0&size=20` 호출
2. 상태 필터 변경 시:
   - `GET /api/admin/orders?page=0&size=20&status={status}` 호출
3. 취소 버튼 클릭:
   - 확인 다이얼로그: "이 주문을 취소하시겠습니까? 사용한 포인트가 환불됩니다."
   - 확인 시: `POST /api/admin/orders/{id}/cancel` 호출
   - 성공 시: Toast(성공 메시지) + 데이터 재조회
   - 실패 시: Toast(에러 메시지)
4. 페이지네이션:
   - 페이지 변경 시 쿼리 파라미터 업데이트 (`?page={page}`)

#### 테이블 컬럼 정의
| 컬럼 | 타입 | 설명 |
|---|---|---|
| 주문번호 | number | orderId |
| 사용자명 | string | userName |
| 상품명 | string | productName |
| 금액 | number | productPrice (1,000p 형식) |
| 상태 | Badge | status (COMPLETED/CANCELLED) |
| 주문일시 | string | createdAt (YYYY-MM-DD HH:mm) |
| 액션 | Button | 취소 버튼 (COMPLETED만) |

---

## 4. 레이아웃 구조

### 4.1 전체 레이아웃

```
┌─────────────────────────────────────────┐
│            Header (고정)                 │
│  [로고] [어드민 대시보드]    [닉네임] [로그아웃]│
├─────────┬───────────────────────────────┤
│         │                               │
│ Sidebar │       Main Content            │
│ (고정)  │      (스크롤 가능)             │
│         │                               │
│ - 대시보드│                               │
│ - 예산   │                               │
│ - 룰렛   │                               │
│ - 상품   │                               │
│ - 주문   │                               │
│         │                               │
└─────────┴───────────────────────────────┘
```

### 4.2 Sidebar 메뉴

| 메뉴 | 경로 | 아이콘 | 설명 |
|---|---|---|---|
| 대시보드 | `/` | LayoutDashboard | 예산 현황 |
| 예산 관리 | `/budget` | DollarSign | 예산 설정 |
| 룰렛 관리 | `/roulette` | RefreshCcw | 룰렛 내역 |
| 상품 관리 | `/products` | Package | 상품 CRUD |
| 주문 관리 | `/orders` | ShoppingCart | 주문 내역 |

### 4.3 Header 요소

- **왼쪽**: 로고 + "어드민 대시보드" 텍스트
- **오른쪽**:
  - 현재 사용자 닉네임 (예: "admin")
  - 로그아웃 버튼

---

## 5. API 연동 명세

### 5.1 API 클라이언트 설정

**파일**: `src/api/client.ts`

```typescript
import axios from 'axios';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true, // 세션 쿠키 전송 (필수!)
  headers: {
    'Content-Type': 'application/json',
  },
});

// 401 에러 시 자동 로그아웃
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### 5.2 공통 응답 타입

```typescript
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error?: {
    code: string;
    message: string;
  };
}
```

### 5.3 API 함수 예시

**파일**: `src/api/products.ts`

```typescript
import { apiClient, ApiResponse } from './client';
import { Product } from '@/types/models';

export async function getProducts() {
  const response = await apiClient.get<ApiResponse<{ products: Product[] }>>('/api/admin/products');
  return response.data;
}

export async function createProduct(data: CreateProductRequest) {
  const response = await apiClient.post<ApiResponse<Product>>('/api/admin/products', data);
  return response.data;
}

export async function updateProduct(id: number, data: UpdateProductRequest) {
  const response = await apiClient.put<ApiResponse<Product>>(`/api/admin/products/${id}`, data);
  return response.data;
}

export async function deleteProduct(id: number) {
  const response = await apiClient.delete<ApiResponse<{ message: string }>>(`/api/admin/products/${id}`);
  return response.data;
}
```

### 5.4 TanStack Query 사용

**useQuery 예시**:
```typescript
const { data, isLoading, error } = useQuery({
  queryKey: ['products'],
  queryFn: getProducts,
});
```

**useMutation 예시**:
```typescript
const queryClient = useQueryClient();
const { toast } = useToast();

const mutation = useMutation({
  mutationFn: createProduct,
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['products'] });
    toast({ title: '상품이 생성되었습니다' });
  },
  onError: (error: any) => {
    toast({
      title: '에러',
      description: error.response?.data?.error?.message || '상품 생성 실패',
      variant: 'destructive'
    });
  },
});
```

---

## 6. TypeScript 타입 정의

### 6.1 모델 타입

**파일**: `src/types/models.ts`

```typescript
export interface User {
  id: number;
  nickname: string;
  role: 'ADMIN' | 'USER';
}

export interface DashboardData {
  budgetDate: string;       // YYYY-MM-DD
  dailyLimit: number;
  remaining: number;
  usedAmount: number;
  participantCount: number;
}

export interface Budget {
  budgetDate: string;       // YYYY-MM-DD
  dailyLimit: number;
  remaining: number;
}

export interface RouletteHistory {
  historyId: number;
  userId: number;
  userName: string;
  originalAmount: number;
  reclaimedAmount: number;
  spinDate: string;         // YYYY-MM-DD
  status: 'ACTIVE' | 'CANCELLED';
}

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stock: number;
  isActive: boolean;
}

export interface Order {
  orderId: number;
  userId: number;
  userName: string;
  productId: number;
  productName: string;
  productPrice: number;
  status: 'COMPLETED' | 'CANCELLED';
  createdAt: string;        // ISO 8601
}
```

### 6.2 API 요청 타입

```typescript
export interface LoginRequest {
  nickname: string;
}

export interface UpdateBudgetRequest {
  dailyLimit: number;
}

export interface CreateProductRequest {
  name: string;
  description: string;
  price: number;
  stock: number;
}

export interface UpdateProductRequest {
  name: string;
  description: string;
  price: number;
  stock: number;
  isActive: boolean;
}
```

---

## 7. 폴더 구조

```
admin/
├── public/                 # 정적 파일
├── src/
│   ├── api/                # API 호출 함수
│   │   ├── client.ts       # Axios 인스턴스
│   │   ├── auth.ts
│   │   ├── dashboard.ts
│   │   ├── budget.ts
│   │   ├── roulette.ts
│   │   ├── products.ts
│   │   └── orders.ts
│   ├── components/
│   │   ├── ui/             # shadcn/ui (자동 생성)
│   │   │   ├── button.tsx
│   │   │   ├── card.tsx
│   │   │   ├── table.tsx
│   │   │   ├── dialog.tsx
│   │   │   ├── form.tsx
│   │   │   ├── input.tsx
│   │   │   ├── label.tsx
│   │   │   ├── toast.tsx
│   │   │   ├── badge.tsx
│   │   │   └── select.tsx
│   │   ├── layout/
│   │   │   ├── Layout.tsx          # 메인 레이아웃
│   │   │   ├── Sidebar.tsx         # 왼쪽 메뉴
│   │   │   └── Header.tsx          # 상단 헤더
│   │   └── shared/
│   │       ├── LoadingSpinner.tsx
│   │       ├── ErrorMessage.tsx
│   │       └── Pagination.tsx
│   ├── contexts/
│   │   └── AuthContext.tsx         # 인증 상태 관리
│   ├── hooks/
│   │   ├── useAuth.ts              # 인증 훅
│   │   └── usePagination.ts        # 페이지네이션 훅
│   ├── pages/
│   │   ├── LoginPage.tsx
│   │   ├── DashboardPage.tsx
│   │   ├── BudgetPage.tsx
│   │   ├── RoulettePage.tsx
│   │   ├── ProductsPage.tsx
│   │   └── OrdersPage.tsx
│   ├── types/
│   │   ├── api.ts                  # API 응답 타입
│   │   └── models.ts               # 비즈니스 모델 타입
│   ├── lib/
│   │   └── utils.ts                # 유틸리티 함수
│   ├── App.tsx
│   ├── main.tsx
│   └── index.css
├── .env.local              # 환경변수 (gitignore)
├── .env.example            # 환경변수 예제
├── .gitignore
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
└── tailwind.config.js
```

---

## 8. 환경변수

### 8.1 로컬 개발 (`.env.local`)

```env
VITE_API_BASE_URL=http://localhost:8080
```

### 8.2 프로덕션 (`.env.production`)

```env
VITE_API_BASE_URL=https://roulette-backend-upmn.onrender.com
```

### 8.3 예제 파일 (`.env.example`)

```env
# API 서버 URL
VITE_API_BASE_URL=http://localhost:8080
```

---

## 9. 보안 고려사항

### 9.1 세션 쿠키 보안

- [x] `withCredentials: true` 설정 (Axios)
- [x] HttpOnly 쿠키 사용 (백엔드에서 설정됨)
- [x] HTTPS 사용 (프로덕션 배포 시)
- [x] CORS 설정 (백엔드에서 프론트엔드 도메인 허용)

### 9.2 인증/인가

- [x] 로그인 시 role=ADMIN 검증
- [x] PrivateRoute로 미인증 사용자 차단
- [x] 401 에러 시 자동 로그아웃
- [x] 세션 만료 시 로그인 페이지 리다이렉트

### 9.3 XSS 방지

- [x] React의 자동 이스케이프 사용
- [x] dangerouslySetInnerHTML 사용 금지
- [x] 사용자 입력 검증 (Zod)

### 9.4 환경변수

- [x] `.env.local` gitignore 처리
- [x] API URL 환경변수로 관리
- [x] 민감 정보 코드에 하드코딩 금지

---

## 10. 테스트 계획

### 10.1 수동 테스트 시나리오

| 시나리오 | 기대 결과 |
|---|---|
| 로그인 (admin) | 대시보드 표시 |
| 로그인 (일반 사용자) | "어드민 권한이 없습니다" 에러 |
| 세션 만료 후 API 호출 | 로그인 페이지 리다이렉트 |
| 대시보드 조회 | 예산 현황 카드 3개 표시 |
| 예산 변경 | 성공 Toast + 데이터 재조회 |
| 룰렛 취소 | 확인 다이얼로그 → 성공 Toast + 재조회 |
| 상품 생성 | Dialog 닫기 + 성공 Toast + 재조회 |
| 상품 수정 | 기존 값 채워진 폼 → 수정 후 재조회 |
| 상품 삭제 (주문 없음) | 성공 Toast + 재조회 |
| 상품 삭제 (주문 있음) | "삭제 불가" 에러 Toast |
| 주문 취소 | 확인 다이얼로그 → 성공 Toast + 재조회 |
| 페이지네이션 | 페이지 변경 시 데이터 재조회 |

### 10.2 성능 테스트

- [ ] 초기 로딩 시간 < 3초
- [ ] API 호출 시 로딩 스피너 표시
- [ ] 페이지 전환 시 부드러운 전환
- [ ] 큰 테이블 (100개 항목) 렌더링 성능 확인

### 10.3 접근성 테스트

- [ ] 키보드 네비게이션 (Tab, Enter)
- [ ] 포커스 표시 명확
- [ ] 버튼 레이블 명확
- [ ] 에러 메시지 스크린 리더 읽기 가능

---

## 11. 빌드 및 배포

### 11.1 로컬 빌드

```bash
cd admin
npm run build
npm run preview  # 프리뷰 서버 실행
```

### 11.2 Vercel 배포 설정

**프로젝트 설정**:
- Root Directory: `admin`
- Build Command: `npm run build`
- Output Directory: `dist`
- Install Command: `npm install`
- Node Version: 18.x

**환경변수** (Vercel Dashboard):
```
VITE_API_BASE_URL=https://roulette-backend-upmn.onrender.com
```

### 11.3 배포 후 확인 사항

- [ ] Health Check: `https://[vercel-url]`
- [ ] 로그인 동작 확인
- [ ] API 연동 확인 (Network 탭)
- [ ] CORS 에러 없는지 확인
- [ ] 세션 쿠키 전송 확인

---

## 12. 주의사항 및 제약사항

### 12.1 필수 준수 사항

1. **세션 쿠키**: `withCredentials: true` 필수
2. **에러 처리**: 모든 useMutation에 onError 핸들러 추가
3. **로딩 상태**: 모든 useQuery에 isLoading 처리
4. **페이지네이션**: page는 0부터 시작 (백엔드 스펙)
5. **날짜 형식**: YYYY-MM-DD (ISO 8601)
6. **금액 표시**: 1,000p 형식 (toLocaleString())
7. **API 연동**: TanStack Query 사용 (axios 직접 호출 금지)
8. **폼 검증**: React Hook Form + Zod 필수
9. **테이블**: shadcn/ui Table 컴포넌트 사용
10. **빈 상태**: 데이터 없을 때 "데이터가 없습니다" 표시

### 12.2 기술적 제약

- React Router v6 사용 (v5 문법 불가)
- TypeScript strict 모드 (any 타입 최소화)
- Tailwind CSS 유틸리티 우선 (인라인 스타일 금지)
- shadcn/ui 컴포넌트 우선 (직접 스타일링 최소화)

### 12.3 성능 고려사항

- 이미지 최적화 (WebP, lazy loading)
- 큰 테이블은 가상 스크롤 고려 (향후)
- API 호출 최소화 (TanStack Query 캐시 활용)
- 번들 사이즈 최소화 (코드 스플리팅)

---

## 13. 마일스톤

### Phase 1: 프로젝트 초기화 (1일)
- [x] Vite 프로젝트 생성
- [x] 패키지 설치 (React Router, TanStack Query, Tailwind, shadcn/ui)
- [x] 폴더 구조 설정
- [x] 환경변수 설정
- [x] API 클라이언트 설정

### Phase 2: 인증 및 레이아웃 (1일)
- [x] AuthContext 구현
- [x] 로그인 페이지
- [x] Layout, Sidebar, Header 컴포넌트
- [x] 라우팅 설정

### Phase 3: 대시보드 및 예산 관리 (1일)
- [x] 대시보드 페이지
- [x] 예산 관리 페이지

### Phase 4: 룰렛 관리 (1일)
- [x] 룰렛 관리 페이지
- [x] 테이블, 필터, 페이지네이션
- [x] 취소 기능

### Phase 5: 상품 관리 (1일)
- [x] 상품 관리 페이지
- [x] CRUD Dialog
- [x] 폼 검증

### Phase 6: 주문 관리 (1일)
- [x] 주문 관리 페이지
- [x] 테이블, 필터, 페이지네이션
- [x] 취소 기능

### Phase 7: 테스트 및 배포 (1일)
- [ ] 수동 테스트 전체 시나리오
- [ ] 빌드 확인
- [ ] Vercel 배포
- [ ] 프로덕션 환경 테스트

**총 예상 기간**: 7일

---

## 14. 참고 문서

- [백엔드 API 명세](./SPEC.md)
- [프로젝트 규칙](../CLAUDE.md)
- [플랜 문서](../.claude/plans/lucky-waddling-sundae.md)
- [shadcn/ui 문서](https://ui.shadcn.com)
- [TanStack Query 문서](https://tanstack.com/query/latest)
- [React Hook Form 문서](https://react-hook-form.com)
- [Zod 문서](https://zod.dev)

---

## 14. 배포 전략 (Vercel)

### 14.1 Vercel 프로젝트 설정

| 설정 | 값 |
|---|---|
| Framework Preset | Vite |
| Root Directory | `admin` |
| Build Command | `npm run build` |
| Output Directory | `dist` |
| Install Command | `npm install` |
| Node.js Version | 20.x |

### 14.2 환경변수

**Production**:
```env
VITE_API_BASE_URL=https://roulette-backend-upmn.onrender.com
```

**Preview** (선택):
```env
VITE_API_BASE_URL=https://roulette-backend-staging.onrender.com
```

### 14.3 배포 워크플로우

1. **자동 배포**: `main` 브랜치 push 시 자동 배포
2. **Preview 배포**: PR 생성 시 Preview URL 자동 생성
3. **롤백**: Vercel 대시보드에서 이전 배포로 즉시 롤백 가능

### 14.4 도메인 설정

- **Production**: `roulette-admin.vercel.app` (기본)
- **커스텀 도메인** (선택): `admin.roulette-vibe.com`

### 14.5 성능 최적화

- **자동 압축**: gzip, brotli 압축 자동 적용
- **CDN**: Vercel Edge Network (전 세계 배포)
- **캐싱**: Static assets 자동 캐싱 (immutable files)
- **이미지 최적화**: Vercel Image Optimization (선택)

---

## 15. TypeScript 타입 안정화 전략

> **스킬 활용**: `/typescript-advanced-types`

### 15.1 고급 타입 패턴

#### Generic API 응답 래퍼
```typescript
// src/types/api.ts
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error?: ApiError;
}

export interface ApiError {
  code: string;
  message: string;
}

// 페이지네이션 응답
export interface PaginatedResponse<T> {
  items: T[];
  pageInfo: PageInfo;
}
```

#### Discriminated Unions (상태 관리)
```typescript
// src/types/query-state.ts
export type QueryState<T> =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'success'; data: T }
  | { status: 'error'; error: Error };

// 사용 예시
function handleQueryState<T>(state: QueryState<T>) {
  switch (state.status) {
    case 'loading':
      return <LoadingSpinner />;
    case 'success':
      return <DataView data={state.data} />;
    case 'error':
      return <ErrorMessage error={state.error} />;
    default:
      return null;
  }
}
```

#### Utility Types 활용
```typescript
// src/types/utils.ts
// Pick만 사용
export type ProductFormData = Pick<Product, 'name' | 'price' | 'stock' | 'description'>;

// Omit으로 제외
export type ProductUpdateData = Omit<Product, 'id' | 'createdAt'>;

// Partial로 선택적
export type ProductFilters = Partial<Pick<Product, 'isActive' | 'minStock'>>;

// Readonly로 불변
export type ReadonlyProduct = Readonly<Product>;
```

#### Template Literal Types (API 경로)
```typescript
// src/types/api-routes.ts
type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE';
type AdminRoute = 'dashboard' | 'budget' | 'products' | 'orders' | 'roulette';

type ApiRoute = `/api/admin/${AdminRoute}`;
// 결과: '/api/admin/dashboard' | '/api/admin/budget' | ...
```

### 15.2 타입 가드 (Type Guards)
```typescript
// src/lib/type-guards.ts
export function isApiError(error: unknown): error is ApiError {
  return (
    typeof error === 'object' &&
    error !== null &&
    'code' in error &&
    'message' in error
  );
}

export function hasData<T>(response: ApiResponse<T>): response is ApiResponse<T> & { data: T } {
  return response.success && response.data !== undefined;
}
```

### 15.3 제네릭 훅 (Generic Hooks)
```typescript
// src/hooks/useApi.ts
export function useApi<T>(
  queryKey: string[],
  queryFn: () => Promise<ApiResponse<T>>,
  options?: UseQueryOptions<ApiResponse<T>>
) {
  return useQuery({
    queryKey,
    queryFn,
    select: (data) => data.data, // 자동으로 data 추출
    ...options,
  });
}

// 사용
const { data: dashboard } = useApi(
  ['dashboard'],
  () => apiClient.get<DashboardData>('/api/admin/dashboard')
);
```

---

## 16. UI 개발 전략

> **스킬 활용**: `/ccpp:frontend`

### 16.1 디자인 시스템 (빅테크 스타일)

#### Stripe 스타일
- **색상**: 보라색 계열 강조색
- **타이포그래피**: Inter 폰트, 명확한 계층 구조
- **그림자**: 섬세한 그림자로 깊이감 표현
- **애니메이션**: 부드러운 페이드 인/아웃

#### Vercel 스타일
- **색상**: 흑백 대비, 강조색은 최소한으로
- **레이아웃**: 넓은 여백, 깔끔한 구성
- **타이포그래피**: 고딕 계열, 가독성 우선
- **인터랙션**: 빠른 피드백, 즉각적 반응

#### Apple 스타일
- **색상**: 차분한 회색 계열, 포인트 색상 절제
- **공간**: 넉넉한 패딩, 시각적 여유
- **컴포넌트**: 둥근 모서리, 부드러운 느낌
- **애니메이션**: 자연스러운 이징, 물리적 느낌

### 16.2 컴포넌트 패턴

#### 합성 패턴 (Composition)
```typescript
// Good: 합성 패턴
<Card>
  <CardHeader>
    <CardTitle>제목</CardTitle>
    <CardDescription>설명</CardDescription>
  </CardHeader>
  <CardContent>내용</CardContent>
  <CardFooter>
    <Button>액션</Button>
  </CardFooter>
</Card>
```

#### Render Props 패턴
```typescript
<DataTable
  data={products}
  columns={columns}
  renderEmpty={() => <EmptyState message="상품이 없습니다" />}
  renderLoading={() => <TableSkeleton />}
/>
```

#### 컴파운드 컴포넌트 (Compound Components)
```typescript
<Dialog>
  <DialogTrigger asChild>
    <Button>열기</Button>
  </DialogTrigger>
  <DialogContent>
    <DialogHeader>
      <DialogTitle>확인</DialogTitle>
      <DialogDescription>정말 삭제하시겠습니까?</DialogDescription>
    </DialogHeader>
    <DialogFooter>
      <Button variant="outline">취소</Button>
      <Button variant="destructive">삭제</Button>
    </DialogFooter>
  </DialogContent>
</Dialog>
```

### 16.3 접근성 (Accessibility)

#### ARIA 속성
```typescript
// 버튼 상태
<Button aria-busy={isLoading} aria-disabled={isDisabled}>
  {isLoading ? '처리 중...' : '제출'}
</Button>

// 에러 메시지
<Input
  aria-invalid={!!error}
  aria-describedby={error ? 'error-message' : undefined}
/>
{error && <span id="error-message" role="alert">{error}</span>}

// 모달
<Dialog aria-labelledby="dialog-title" aria-describedby="dialog-description">
  <DialogTitle id="dialog-title">제목</DialogTitle>
  <DialogDescription id="dialog-description">설명</DialogDescription>
</Dialog>
```

#### 키보드 네비게이션
- **Tab**: 포커스 이동
- **Enter/Space**: 버튼 활성화
- **Escape**: 모달/드롭다운 닫기
- **Arrow Keys**: 리스트/테이블 네비게이션

### 16.4 스타일 가이드

#### Tailwind CSS 클래스 순서
1. Layout: `flex`, `grid`, `block`
2. Positioning: `relative`, `absolute`, `top-0`
3. Sizing: `w-full`, `h-screen`
4. Spacing: `p-4`, `m-2`, `gap-4`
5. Typography: `text-lg`, `font-bold`
6. Colors: `bg-white`, `text-gray-900`
7. Effects: `shadow-lg`, `rounded-md`
8. Transitions: `transition`, `duration-200`

#### 색상 팔레트 (shadcn/ui)
```css
:root {
  --background: 0 0% 100%;       /* 흰색 */
  --foreground: 222.2 84% 4.9%;  /* 거의 검정 */
  --primary: 222.2 47.4% 11.2%;  /* 어두운 파랑 */
  --secondary: 210 40% 96.1%;    /* 밝은 회색 */
  --muted: 210 40% 96.1%;        /* 약간 어두운 회색 */
  --accent: 210 40% 96.1%;       /* 강조색 */
  --destructive: 0 84.2% 60.2%;  /* 빨강 */
  --border: 214.3 31.8% 91.4%;   /* 연한 회색 테두리 */
}
```

---

## 17. 다음 단계

명세서 작성이 완료되었습니다. 새 세션에서 다음 명령어로 구현을 시작하세요:

```
docs/ADMIN_SPEC.md 읽고 구현 시작해줘
```

구현 완료 후 검증:

```
/ccpp:spec-verify
```
