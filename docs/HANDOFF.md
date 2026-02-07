# 작업 인계 문서

**날짜**: 2026-02-07
**세션**: 6차 세션
**마지막 작업**: 어드민 웹 - 예산 관리 페이지 구현 완료

---

## 완료된 작업

### 백엔드
- [x] Spring Boot + Kotlin 기반 API 서버 구현
- [x] PostgreSQL 16 + JPA 구현
- [x] 세션 기반 인증 (role: USER/ADMIN 분리)
- [x] CORS 설정 (allowedOriginPatterns 사용)
- [x] 세션 쿠키 설정 (Secure=false, SameSite=none - 로컬 개발용)
- [x] Render 배포 (https://roulette-backend-upmn.onrender.com)
- [x] GitHub Actions CI/CD 구축
- [x] 어드민 API 전체 구현
  - [x] 대시보드 API (`GET /api/admin/dashboard`)
  - [x] 예산 API (`GET/PUT /api/admin/budget`)
  - [x] 룰렛 내역 API (`GET /api/admin/roulette/history`)
  - [x] 룰렛 취소 API (`POST /api/admin/roulette/{id}/cancel`)

### 어드민 웹 프론트엔드
- [x] Vite + React 18 + TypeScript 환경 구축
- [x] TanStack Query + React Router 설정
- [x] shadcn/ui + Tailwind CSS 설정
- [x] 로그인 페이지 구현 (`/login`)
- [x] 대시보드 페이지 구현 (`/`)
  - 4개 통계 카드 (일일 예산, 남은 예산, 사용 예산, 참여자 수)
  - 예산 소진율 Progress Bar
  - 로딩/에러 상태 처리
- [x] 예산 관리 페이지 구현 (`/budget`)
  - 현재 예산 표시 (날짜, 한도, 남은 예산, 사용 예산)
  - 예산 변경 폼 (React Hook Form + Zod 검증)
  - 룰렛 참여 내역 테이블 (페이지네이션, 날짜 필터)
  - 룰렛 취소 기능 (확인 Dialog)
- [x] UI 컴포넌트 구현
  - Card, Button, Input, Label
  - Skeleton (로딩 상태)
  - Progress (진행률 바)
  - Badge (상태 표시)
  - Table (데이터 테이블)
  - Dialog (확인 모달)

### 문서화
- [x] CLAUDE.md 업데이트 (어드민 웹 섹션 추가)
- [x] ADMIN_SPEC.md 작성 (배포/타입/UI 전략)
- [x] PROMPT.md 업데이트 (세션 6 기록)

---

## 진행 중인 작업

**없음** - 예산 관리 페이지가 완전히 완료됨

---

## 다음에 해야 할 작업

### 우선순위 1: 상품 관리 페이지 (`/products`)
**예상 소요 시간**: 2-3시간

**요구사항** (docs/ADMIN_SPEC.md 섹션 3.5 참조):
1. **상품 목록 테이블**
   - API: `GET /api/admin/products`
   - 컬럼: ID, 상품명, 설명, 가격, 재고, 상태, 액션
   - 상태 Badge: ACTIVE (초록), INACTIVE (회색)
   - 재고 0: 빨간색 배지

2. **상품 추가/수정 Dialog**
   - 상품명 (1~100자)
   - 설명 (1~500자, textarea)
   - 가격 (1p 이상)
   - 재고 (0 이상)
   - 상태 (ACTIVE/INACTIVE, Select)
   - React Hook Form + Zod 검증

3. **상품 삭제**
   - 확인 Dialog
   - API: `DELETE /api/admin/products/{id}`
   - 주문 내역 있으면 삭제 불가 (에러 처리)

**필요한 UI 컴포넌트**:
- Select (드롭다운) - 아직 없음, 생성 필요
- Textarea - Input 컴포넌트 확장 또는 별도 생성

**구현 순서**:
1. Select 컴포넌트 생성 (`src/components/ui/select.tsx`)
2. Textarea 컴포넌트 생성 (`src/components/ui/textarea.tsx`)
3. 상품 API 함수 작성 (`src/api/products.ts`)
4. ProductsPage 컴포넌트 작성 (`src/pages/ProductsPage.tsx`)
5. App.tsx 라우팅 연결
6. 빌드 & 테스트

---

### 우선순위 2: 주문 관리 페이지 (`/orders`)
**예상 소요 시간**: 1-2시간

**요구사항** (docs/ADMIN_SPEC.md 섹션 3.6 참조):
1. **주문 목록 테이블**
   - API: `GET /api/admin/orders?page={page}&size={size}&status={status}`
   - 컬럼: ID, 사용자명, 상품명, 수량, 총 금액, 날짜, 상태, 액션
   - 상태 필터 (Select): 전체, COMPLETED, CANCELLED
   - 페이지네이션

2. **주문 취소**
   - 확인 Dialog
   - API: `POST /api/admin/orders/{id}/cancel`
   - 성공 시 포인트 환불 + 재고 복구

**구현 순서**:
1. 주문 API 함수 작성 (`src/api/orders.ts`)
2. OrdersPage 컴포넌트 작성 (`src/pages/OrdersPage.tsx`)
3. App.tsx 라우팅 연결
4. 빌드 & 테스트

---

### 우선순위 3: 어드민 웹 배포 (Vercel)
**예상 소요 시간**: 30분

**작업**:
1. Vercel 프로젝트 생성
2. 환경변수 설정
   - `VITE_API_BASE_URL=https://roulette-backend-upmn.onrender.com`
3. 빌드 설정
   - Framework Preset: Vite
   - Root Directory: `admin`
   - Build Command: `npm run build`
   - Output Directory: `dist`
4. 배포 후 검증
5. 백엔드 쿠키 설정 원복 (`Secure=true`)

---

### 우선순위 4: 사용자 웹 프론트엔드 (frontend/)
**예상 소요 시간**: 4-6시간

**구현할 페이지**:
1. 로그인 페이지
2. 룰렛 참여 페이지
3. 포인트 내역 페이지
4. 상품 목록 페이지
5. 상품 상세/주문 페이지
6. 주문 내역 페이지

---

### 우선순위 5: Flutter 모바일 앱
**예상 소요 시간**: 2-3시간

**구현**:
- WebView로 사용자 웹 렌더링
- 앱 기본 설정
- Android/iOS 빌드

---

## 주의사항

### 백엔드
- ⚠️ **세션 쿠키 설정 임시 조치**
  - 현재: `Secure=false`, `SameSite=none` (로컬 개발용)
  - TODO: 어드민 Vercel 배포 후 `Secure=true`로 원복
  - 위치: `backend/src/main/resources/application-prod.yml`

- ⚠️ **CORS 설정**
  - `allowedOriginPatterns` 사용 (와일드카드 지원)
  - 로컬: `http://localhost:*`
  - 배포: `https://*.vercel.app`
  - 위치: `backend/src/main/kotlin/com/roulette/config/SecurityConfig.kt`

### 프론트엔드
- ⚠️ **Optional chaining 필수**
  - API 응답 데이터는 항상 `?.` 사용
  - 예: `history?.items && history.items.length > 0`
  - 이유: 로딩 중이거나 에러 시 `undefined` 가능

- ⚠️ **Button 컴포넌트 variant**
  - 지원: `default`, `destructive`, `outline`, `secondary`, `ghost`, `link`
  - 지원: `size="sm"`, `size="lg"`, `size="icon"`

- ⚠️ **TanStack Query 패턴**
  - `queryKey`는 배열로, 필터/페이지 파라미터 포함
  - `invalidateQueries`로 관련 쿼리 재조회
  - 예: `queryClient.invalidateQueries({ queryKey: ['budget'] })`

### 환경변수
- 로컬: `.env.local` (gitignore 처리됨)
- 배포: Vercel/Render 환경변수 설정
- 현재 어드민 `.env.local`:
  ```
  VITE_API_BASE_URL=https://roulette-backend-upmn.onrender.com
  ```

---

## 알려진 버그

**없음** - 현재 구현된 기능은 모두 정상 작동

---

## 관련 파일

### 백엔드
- `backend/src/main/kotlin/com/roulette/config/SecurityConfig.kt` - CORS/인증 설정
- `backend/src/main/resources/application-prod.yml` - 프로덕션 설정 (세션 쿠키)
- `backend/src/main/kotlin/com/roulette/controller/AdminController.kt` - 어드민 API

### 어드민 웹
- `admin/src/App.tsx` - 라우팅 설정
- `admin/src/api/client.ts` - Axios 클라이언트 (withCredentials)
- `admin/src/api/dashboard.ts` - 대시보드 API
- `admin/src/api/budget.ts` - 예산 API
- `admin/src/api/roulette.ts` - 룰렛 API
- `admin/src/pages/DashboardPage.tsx` - 대시보드 페이지
- `admin/src/pages/BudgetPage.tsx` - 예산 관리 페이지 (룰렛 내역 포함)
- `admin/src/components/layout/Sidebar.tsx` - 네비게이션 메뉴
- `admin/src/components/ui/*` - shadcn/ui 컴포넌트들

### 문서
- `docs/SPEC.md` - 전체 프로젝트 명세
- `docs/ADMIN_SPEC.md` - 어드민 웹 상세 명세
- `docs/PROMPT.md` - AI 대화 로그 (원문)
- `CLAUDE.md` - 프로젝트 가이드 & 규칙
- `docs/HANDOFF.md` - 이 파일

---

## 마지막 상태

### Git
- **브랜치**: `main`
- **마지막 커밋**:
  - `bebadb8` - "fix: 세션 쿠키 설정 수정 (로컬 개발용)"
  - `7207504` - "fix: CORS 설정 개선 - allowedOriginPatterns 사용"
- **푸시 상태**: origin/main과 동기화됨
- **Unstaged 변경사항**:
  - `.claude/settings.local.json`
  - `admin/package-lock.json`, `admin/package.json`
  - `admin/src/` (여러 새 파일들 - 아직 커밋 안 됨)
  - `docs/PROMPT.md` (세션 6 업데이트)
  - `docs/HANDOFF.md` (이 파일)

### 빌드 상태
- **백엔드**: ✅ 빌드 성공 (Gradle)
- **어드민**: ✅ 빌드 성공 (Vite)
  - 번들 크기: 468KB (gzip: 149KB)

### 배포 상태
- **백엔드**: ✅ Render에 배포됨
  - URL: https://roulette-backend-upmn.onrender.com
  - Health: `/actuator/health` 정상
- **어드민**: ❌ 아직 배포 안 됨 (로컬만)
  - 로컬: http://localhost:5174

### 테스트 상태
- **백엔드**: ⚠️ 테스트 없음 (`-x test`로 빌드)
- **어드민**: ⚠️ 테스트 없음

---

## 다음 세션 시작 방법

### Option 1: HANDOFF.md로 시작 (권장)
```
HANDOFF.md 읽고 이어서 작업해줘. 상품 관리 페이지부터 시작하자.
```

### Option 2: 특정 작업 지정
```
HANDOFF.md 확인하고, 어드민 웹 Vercel 배포부터 시작해줘.
```

### Option 3: 새로운 작업
```
HANDOFF.md 읽고, 사용자 웹 프론트엔드 구현 시작하자.
```

---

## 컨텍스트 정보

- **현재 토큰 사용량**: ~98k / 200k (49%)
- **Compact 사용 횟수**: 1회
- **권장 조치**: 아직 여유 있음, compact 불필요
- **다음 세션**: 새 세션 시작 권장 (컨텍스트 클린)

---

**작성자**: Claude Sonnet 4.5
**작성일**: 2026-02-07 20:20 KST
