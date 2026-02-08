# Frontend Deployment Guide

## Vercel 배포 가이드

### 1. 사전 준비

**필수:**
- GitHub 계정
- Vercel 계정 (GitHub 연동)
- 백엔드 배포 완료 (https://roulette-backend-upmn.onrender.com)

### 2. GitHub에 푸시

```bash
cd /Users/kimhyeryoung/Desktop/roulette-vibe
git add -A
git commit -m "chore: Vercel 배포 설정"
git push origin main
```

### 3. Vercel 프로젝트 생성

#### 방법 1: Vercel Dashboard (권장)

1. **https://vercel.com** 접속
2. **New Project** 클릭
3. GitHub 저장소 선택: `roulette-vibe`
4. **Framework Preset**: Vite 선택됨 (자동 감지)
5. **Root Directory**: `frontend` 입력 ⚠️ **중요!**
6. **Build Command**: `npm run build` (기본값)
7. **Output Directory**: `dist` (기본값)
8. **Install Command**: `npm install` (기본값)

#### 환경변수 설정

**Environment Variables 섹션에서:**

| Name | Value |
|------|-------|
| `VITE_API_BASE_URL` | `https://roulette-backend-upmn.onrender.com` |

**Environment**: Production, Preview, Development 모두 체크

9. **Deploy** 클릭

#### 방법 2: Vercel CLI

```bash
# Vercel CLI 설치 (최초 1회)
npm install -g vercel

# 로그인
vercel login

# 프로젝트 디렉토리로 이동
cd frontend

# 배포
vercel

# 프로덕션 배포
vercel --prod
```

**CLI에서 환경변수 설정:**
```bash
vercel env add VITE_API_BASE_URL production
# 입력: https://roulette-backend-upmn.onrender.com
```

### 4. 배포 확인

배포 완료 후:
1. Vercel이 제공한 URL 접속 (예: `https://roulette-vibe-frontend.vercel.app`)
2. 로그인 테스트
3. 룰렛 돌리기
4. 상품 구매
5. 주문 내역 확인

### 5. 커스텀 도메인 설정 (선택)

1. Vercel Dashboard → 프로젝트 선택
2. **Settings** → **Domains**
3. 도메인 입력 후 DNS 설정

### 6. 자동 배포

**설정 완료 후:**
- `main` 브랜치에 push → 자동으로 프로덕션 배포
- PR 생성 → 자동으로 Preview 배포

### 7. 환경별 API URL

| 환경 | API URL | 사용처 |
|------|---------|--------|
| 로컬 개발 (백엔드 로컬) | `http://localhost:8080` | `.env.local` |
| 로컬 개발 (백엔드 배포) | `https://roulette-backend-upmn.onrender.com` | `.env.local` |
| Vercel 배포 | `https://roulette-backend-upmn.onrender.com` | Vercel 환경변수 |

### 8. 성능 최적화

**이미 적용된 최적화:**
- ✅ Asset 캐싱 (1년, immutable)
- ✅ 보안 헤더 (X-Content-Type-Options, X-Frame-Options, X-XSS-Protection)
- ✅ SPA 라우팅 (rewrites)
- ✅ Code splitting (Vite 자동)
- ✅ Tree shaking (Vite 자동)
- ✅ Minification (Vite 자동)

**추가 최적화 가능:**
- Image optimization (next/image 사용 시)
- Web vitals monitoring (Vercel Analytics)

### 9. 트러블슈팅

**문제: 404 에러 (라우팅)**
- **원인**: SPA 라우팅 미설정
- **해결**: `vercel.json`의 rewrites 확인

**문제: API 연결 실패**
- **원인**: 환경변수 미설정 또는 CORS
- **해결**: Vercel 환경변수 확인, 백엔드 CORS 설정 확인

**문제: 빌드 실패**
- **원인**: TypeScript 에러, 의존성 문제
- **해결**: 로컬에서 `npm run build` 테스트

**문제: 느린 첫 로딩**
- **원인**: Render 무료 플랜 (15분 후 sleep)
- **해결**: 첫 요청 시 15초 정도 대기 (정상)

### 10. 로그 확인

**Vercel Dashboard:**
1. 프로젝트 선택
2. **Deployments** 탭
3. 특정 배포 클릭 → **Logs** 확인

**실시간 로그:**
```bash
vercel logs
```

### 11. 롤백

**문제 발생 시 이전 버전으로 롤백:**

1. Vercel Dashboard → **Deployments**
2. 정상 작동하던 이전 배포 선택
3. **⋯ 메뉴** → **Promote to Production**

### 12. 환경별 빌드 최적화

**프로덕션 빌드:**
```bash
npm run build
```

**미리보기:**
```bash
npm run preview
# http://localhost:4173 접속
```

## 체크리스트

배포 전 확인:
- [ ] `.env.local`이 `.gitignore`에 포함됨
- [ ] `vercel.json` 설정 완료
- [ ] 로컬에서 `npm run build` 성공
- [ ] 백엔드 API 정상 작동 확인
- [ ] GitHub에 최신 코드 push

배포 후 확인:
- [ ] Vercel 환경변수 설정 완료
- [ ] 배포된 URL 접속 가능
- [ ] 로그인/로그아웃 정상 작동
- [ ] 모든 페이지 접근 가능
- [ ] API 호출 정상 작동
