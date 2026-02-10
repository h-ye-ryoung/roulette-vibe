# Point Roulette 🎰

일일 예산 제약을 동시성 환경에서도 정확히 지키는 포인트 룰렛 서비스

[![Backend](https://img.shields.io/badge/Backend-Spring_Boot-green)](https://roulette-backend-upmn.onrender.com)
[![Frontend](https://img.shields.io/badge/Frontend-React_18-blue)](https://roulette-vibe.vercel.app)
[![Admin](https://img.shields.io/badge/Admin-React_18-orange)](https://roulette-admin.vercel.app)
[![Mobile](https://img.shields.io/badge/Mobile-Flutter-cyan)](mobile/)

## 바로가기 (Link)
- **Swagger UI** : https://roulette-backend-upmn.onrender.com/swagger-ui/swagger-ui/index.html#/
- **백엔드 URL** : https://roulette-backend-upmn.onrender.com
- **사용자 웹**: https://roulette-vibe.vercel.app
- **어드민 웹**: https://roulette-admin.vercel.app
- **APK 다운로드 링크**: https://drive.google.com/drive/folders/16d2wLRy9PqIFL_vjXGqEmrn5UOGqS2hA?usp=drive_link
- **github**: https://github.com/h-ye-ryoung/roulette-vibe
---

## 문서화 항목 

| 문서                   | 내용                  |
|----------------------|---------------------|
| **CLAUDE.md**        | 프로젝트 최상단 정책, 코딩 컨벤션 |
| **docs/SPEC.md**     | 개발 기준 문서            |
| **docs/AI_REPORT.md** | AI 활용 리포트           |
| **docs/HANDOFF.md**  | 작업 인계 문서            |너무 많아;
| **docs/PROMPT.md**   | 전체 대화 기록            |
---


## 프로젝트 개요

**Point Roulette**는 다음 핵심 제약을 동시성 환경에서도 정확히 지키는 풀스택 서비스입니다:

- ✅ **1일 1회 제약**: 유저별 하루 1회만 룰렛 참여 가능
- ✅ **일일 예산 제약**: 100,000p 일일 예산, 소진 시 지급 불가
- ✅ **포인트 유효기간**: 지급일 + 30일
- ✅ **FIFO 차감**: 만료 임박 포인트부터 우선 차감
- ✅ **동시성 안전**: DB 트랜잭션/락으로 정합성 보장

### 주요 기능

| 역할 | 기능 |
|------|------|
| **사용자** | 룰렛 참여 (100~1000p), 포인트 내역 조회, 상품 구매, 주문 내역 |
| **어드민** | 대시보드, 예산 관리, 상품 CRUD, 주문 취소/환불, 룰렛 취소 |

### 사용 스킬셋 / 툴 출처

- https://github.com/jh941213/my-claude-code-asset
-  [https://github.com/toy-crane/cc-playbook/blob/main/.claude/commands/clarify.md](https://github.com/toy-crane/cc-playbook/blob/main/.claude/commands/clarify.md)
- https://github.com/tuannvm/codex-mcp-server

### 고려한 핵심 쟁점
| 영역    | 쟁점                   |
| ----- | -------------------- |
| 운영 정책 | 상품 Hard Delete 조건    |
| 운영 정책 | 주문 취소 vs 룰렛 취소 정책 분리 |
| 포인트   | 포인트 환불 vs 포인트 회수 구분  |
| 포인트   | 포인트 채권 분리 모델 도입      |


---

## 기술 스택

### Backend
- **Spring Boot 3.5.0** (Kotlin 2.0.21, Java 21 LTS)
- **PostgreSQL 16** (Neon - Production)
- **Spring Security** (세션 기반 인증)
- **Swagger UI** (API 문서)
- **Render** (배포)

### Frontend (사용자 웹 / 어드민 웹)
- **React 18** + **TypeScript** + **Vite**
- **TanStack Query** (서버 상태)
- **React Router v6** (라우팅)
- **shadcn/ui** + **Tailwind CSS** (UI)
- **Vercel** (배포)

### Mobile
- **Flutter 3.10+** (Dart 3.10+)
- **webview_flutter 4.10** (WebView 래퍼)
- **iOS 11+** / **Android 5.0+ (API 21+)** 지원


---

## 모바일 테스트 가이드

### 사전 요구 스택

#### iOS
- **Flutter SDK 3.10+**
- **Xcode 14+** (iOS 시뮬레이터)
- **CocoaPods** (iOS 의존성 관리)

#### Android
- **Flutter SDK 3.10+**
- **Android Studio** (Android 에뮬레이터)
- **Android SDK API 21+**

### 실행 방식 (iOS / Android 동일)

각 애뮬레이터 실행 후 명령어 작성
```bash
flutter run
```


### 모바일 주요 기능

1. **WebView 렌더링**
   - Frontend 웹앱 렌더링
   - 로딩 상태 표시 (Purple 스피너)
2. **네비게이션**
   - 뒤로가기: WebView 히스토리 확인 → `goBack()` or 앱 종료
3. **세션 유지**
   - 쿠키 자동 저장 → 로그인 상태 유지
   - 앱 종료 후 재실행해도 로그인 유지
---

## 프로젝트 구조

```
roulette-vibe/
├── backend/          # Spring Boot API (Kotlin)
├── frontend/         # 사용자 웹 (React + Vite)
├── admin/            # 어드민 웹 (React + Vite)
├── mobile/           # Flutter WebView 앱 (iOS/Android)
├── docs/             # 명세서 및 문서
│   ├── SPEC.md       # 기술 명세
│   ├── HANDOFF.md    # 작업 인계
│   └── PROMPT.md     # AI 작업 로그
├── .github/
│   └── workflows/    # CI/CD (Backend)
├── CLAUDE.md         # 프로젝트 규칙 및 정책
└── README.md         # 이 문서
```
