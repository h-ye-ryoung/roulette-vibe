# Admin Web Frontend - 초기 설정 완료

## 프로젝트 구조

```
admin/
├── src/
│   ├── api/
│   │   └── client.ts          # Axios 인스턴스 (withCredentials: true)
│   ├── components/
│   │   ├── ui/                # shadcn/ui 컴포넌트
│   │   │   ├── button.tsx
│   │   │   ├── card.tsx
│   │   │   └── input.tsx
│   │   ├── layout/            # 레이아웃 컴포넌트
│   │   │   ├── Layout.tsx     # 메인 레이아웃
│   │   │   ├── Sidebar.tsx    # 사이드바 네비게이션
│   │   │   └── Header.tsx     # 상단 헤더
│   │   └── shared/            # 공통 컴포넌트 (TODO)
│   ├── pages/
│   │   ├── LoginPage.tsx      # 로그인 화면 (완료)
│   │   └── DashboardPage.tsx  # 대시보드 (스켈레톤)
│   ├── contexts/
│   │   └── AuthContext.tsx    # 인증 컨텍스트
│   ├── hooks/                 # 커스텀 훅 (TODO)
│   ├── types/
│   │   └── models.ts          # TypeScript 타입 정의
│   ├── lib/
│   │   └── utils.ts           # 유틸리티 (cn 함수)
│   ├── App.tsx                # 라우팅 설정
│   ├── main.tsx
│   └── index.css              # Tailwind CSS 설정
├── .env.example               # 환경변수 예제
├── .env.local                 # 로컬 환경변수
├── tailwind.config.js         # Tailwind 설정
├── tsconfig.app.json          # TypeScript 설정 (path alias)
├── vite.config.ts             # Vite 설정
└── package.json
```

## 설치된 패키지

### 핵심 의존성
- **react** 18.3.1
- **react-router-dom** 7.5.0 (라우팅)
- **@tanstack/react-query** 6.0.13 (서버 상태 관리)
- **axios** 1.7.9 (API 호출)
- **react-hook-form** 7.54.2 (폼 처리)
- **zod** 3.24.1 (스키마 검증)

### UI 라이브러리
- **tailwindcss** 3.4.17 (스타일링)
- **tailwindcss-animate** (애니메이션)
- **class-variance-authority** (컴포넌트 variants)
- **clsx** + **tailwind-merge** (className 유틸리티)
- **lucide-react** (아이콘)

## 완료된 작업

### ✅ 1. 프로젝트 초기화
- Vite + React 18 + TypeScript 설정
- 폴더 구조 생성
- Path alias 설정 (`@/*` → `./src/*`)

### ✅ 2. 스타일링 설정
- Tailwind CSS v3 설치 및 설정
- shadcn/ui 스타일 변수 설정
- 기본 UI 컴포넌트 (Button, Card, Input)

### ✅ 3. API 클라이언트
- Axios 인스턴스 생성
- `withCredentials: true` 설정 (세션 쿠키)
- 401 에러 시 자동 로그인 페이지 리다이렉트

### ✅ 4. 인증 시스템
- AuthContext 생성
- 세션 확인 (`/api/auth/me`)
- 로그인/로그아웃 함수
- PrivateRoute 컴포넌트

### ✅ 5. 라우팅 구조
- React Router v6 설정
- 로그인 페이지 (`/login`)
- 보호된 라우트 (대시보드, 예산, 룰렛, 상품, 주문)

### ✅ 6. 기본 UI 구현
- LoginPage (완료)
- DashboardPage (스켈레톤)
- Sidebar 네비게이션
- Header (닉네임, 로그아웃 버튼)

### ✅ 7. TypeScript 타입 정의
- User, DashboardData, BudgetData
- Product, Order, RouletteHistory
- PageInfo (페이지네이션)

### ✅ 8. 빌드 검증
- TypeScript 컴파일 통과
- Vite 빌드 성공

## 환경변수 설정

`.env.local` 파일:
```env
VITE_API_BASE_URL=http://localhost:8080
```

배포 시 (Vercel):
```env
VITE_API_BASE_URL=https://roulette-backend-upmn.onrender.com
```

## 개발 서버 실행

```bash
npm run dev
```

브라우저에서 `http://localhost:5173` 접속

## 빌드

```bash
npm run build
npm run preview  # 빌드 결과 미리보기
```

## 다음 단계

### 1. API 함수 작성 (`src/api/`)
- `auth.ts` - 로그인/로그아웃/세션 확인
- `dashboard.ts` - 대시보드 데이터
- `budget.ts` - 예산 조회/변경
- `roulette.ts` - 룰렛 내역/취소
- `products.ts` - 상품 CRUD
- `orders.ts` - 주문 조회/취소

### 2. 페이지 구현
- [ ] DashboardPage - TanStack Query로 데이터 연동
- [ ] BudgetPage - 예산 조회/변경 폼
- [ ] RoulettePage - 테이블 + 페이지네이션 + 취소 버튼
- [ ] ProductsPage - CRUD 전체 (생성/수정/삭제 모달)
- [ ] OrdersPage - 테이블 + 필터 + 취소 버튼

### 3. shadcn/ui 컴포넌트 추가
```bash
npx shadcn@latest add table
npx shadcn@latest add dialog
npx shadcn@latest add form
npx shadcn@latest add label
npx shadcn@latest add toast
npx shadcn@latest add badge
npx shadcn@latest add select
```

### 4. 공통 컴포넌트 작성
- `LoadingSpinner.tsx`
- `ErrorMessage.tsx`
- `Pagination.tsx`

### 5. 커스텀 훅
- `usePagination.ts`

## 참고사항

### API 호출 패턴
```typescript
// TanStack Query 사용
const { data, isLoading, error } = useQuery({
  queryKey: ['dashboard'],
  queryFn: async () => {
    const response = await apiClient.get('/api/admin/dashboard');
    return response.data.data;
  },
});
```

### Mutation 패턴
```typescript
const mutation = useMutation({
  mutationFn: async (data) => {
    const response = await apiClient.post('/api/admin/products', data);
    return response.data.data;
  },
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['products'] });
  },
});
```

### 폼 검증 (React Hook Form + Zod)
```typescript
const schema = z.object({
  name: z.string().min(1, '필수 입력'),
  price: z.number().min(1, '1 이상'),
});

const form = useForm({
  resolver: zodResolver(schema),
});
```

## 주의사항

1. **세션 쿠키**: `apiClient`는 이미 `withCredentials: true` 설정됨
2. **ADMIN 권한 확인**: AuthContext에서 role 검증
3. **에러 처리**: 모든 API 호출에 try-catch 또는 onError 핸들러
4. **로딩 상태**: 모든 useQuery에 isLoading 처리 필수
5. **페이지네이션**: page는 0부터 시작 (백엔드 API 스펙)
6. **날짜 형식**: YYYY-MM-DD (ISO 8601)
7. **금액 표시**: `toLocaleString()` 사용 (예: 1,000p)
