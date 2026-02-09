# Point Roulette 🎰

일일 예산 제약을 동시성 환경에서도 정확히 지키는 포인트 룰렛 서비스

[![Backend](https://img.shields.io/badge/Backend-Spring_Boot-green)](https://roulette-backend-upmn.onrender.com)
[![Frontend](https://img.shields.io/badge/Frontend-React_18-blue)](https://roulette-vibe.vercel.app)
[![Admin](https://img.shields.io/badge/Admin-React_18-orange)](https://roulette-admin.vercel.app)
[![Mobile](https://img.shields.io/badge/Mobile-Flutter-cyan)](mobile/)

---

## 📋 목차

- [프로젝트 개요](#프로젝트-개요)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [시작하기](#시작하기)
  - [사전 요구사항](#사전-요구사항)
  - [백엔드 실행](#백엔드-실행)
  - [프론트엔드 실행](#프론트엔드-실행)
  - [iOS 앱 실행](#ios-앱-실행)
  - [Android 앱 실행](#android-앱-실행)
- [배포](#배포)
- [문서](#문서)

---

## 프로젝트 개요

**Point Roulette**는 다음 핵심 제약을 동시성 환경에서도 정확히 지키는 풀스택 서비스입니다:

- ✅ **1일 1회 제약**: 유저별 하루 1회만 룰렛 참여 가능
- ✅ **일일 예산 제약**: 100,000p 일일 예산, 소진 시 지급 불가
- ✅ **포인트 유효기간**: 지급일 + 30일 (만료 포인트 자동 필터링)
- ✅ **FIFO 차감**: 만료 임박 포인트부터 우선 차감
- ✅ **동시성 안전**: DB 트랜잭션/락으로 정합성 보장

### 주요 기능

| 역할 | 기능 |
|------|------|
| **사용자** | 룰렛 참여 (100~1000p), 포인트 내역 조회, 상품 구매, 주문 내역 |
| **어드민** | 대시보드, 예산 관리, 상품 CRUD, 주문 취소/환불, 룰렛 취소 |

---

## 기술 스택

### Backend
- **Spring Boot 3.5.0** (Kotlin 2.0.21, Java 21 LTS)
- **PostgreSQL 16** (Neon - Production)
- **Spring Security** (세션 기반 인증)
- **Swagger UI** (API 문서)
- **Render** (배포)

### Frontend (사용자 웹)
- **React 18** + **TypeScript** + **Vite**
- **TanStack Query** (서버 상태)
- **React Router v6** (라우팅)
- **shadcn/ui** + **Tailwind CSS** (UI)
- **Vercel** (배포)

### Admin (어드민 웹)
- **React 18** + **TypeScript** + **Vite**
- **TanStack Query** (서버 상태)
- **React Hook Form** + **Zod** (폼 처리)
- **shadcn/ui** + **Tailwind CSS** (UI)
- **Vercel** (배포)

### Mobile
- **Flutter 3.10+** (Dart 3.10+)
- **webview_flutter 4.10** (WebView 래퍼)
- **iOS 11+** / **Android 5.0+ (API 21+)** 지원

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
│   ├── ADMIN_SPEC.md # 어드민 명세
│   ├── HANDOFF.md    # 작업 인계
│   └── PROMPT.md     # AI 작업 로그
├── .github/
│   └── workflows/    # CI/CD (Backend)
├── CLAUDE.md         # 프로젝트 규칙 및 정책
└── README.md         # 이 문서
```

---

## 시작하기

### 사전 요구사항

#### 공통
- **Git** (버전 관리)
- **Node.js 18+** (프론트엔드)
- **npm** or **yarn** (패키지 매니저)

#### 백엔드
- **Java 21 LTS** (JDK)
- **Gradle 8.x** (빌드 도구, Wrapper 포함)
- **PostgreSQL 16** (로컬 DB) - Docker Compose 권장

#### 모바일 (iOS)
- **macOS** (iOS 빌드 필수)
- **Flutter SDK 3.10+**
- **Xcode 14+** (iOS 시뮬레이터)
- **CocoaPods** (iOS 의존성 관리)

#### 모바일 (Android)
- **Flutter SDK 3.10+**
- **Android Studio** (Android 에뮬레이터)
- **Android SDK API 21+**

---

### 백엔드 실행

#### 1. 로컬 데이터베이스 실행 (Docker Compose)

```bash
# PostgreSQL 16 컨테이너 시작
docker compose up -d

# 확인
docker compose ps
```

#### 2. 백엔드 실행

```bash
cd backend

# Gradle Wrapper로 빌드 및 실행
./gradlew bootRun

# 또는 IDE(IntelliJ IDEA)에서 실행
```

**API 서버**: http://localhost:8080
**Swagger UI**: http://localhost:8080/swagger-ui.html

---

### 프론트엔드 실행

#### 사용자 웹

```bash
cd frontend

# 의존성 설치
npm install

# 개발 서버 실행
npm run dev
```

**로컬 서버**: http://localhost:5173

#### 어드민 웹

```bash
cd admin

# 의존성 설치
npm install

# 개발 서버 실행
npm run dev
```

**로컬 서버**: http://localhost:5174

---

### iOS 앱 실행

#### 사전 요구사항

**macOS에서만 실행 가능**합니다. 다음을 설치해야 합니다:

1. **Flutter SDK**
   ```bash
   # Homebrew로 설치
   brew install --cask flutter

   # PATH 추가 (필요 시)
   export PATH="$PATH:`pwd`/flutter/bin"
   ```

2. **Xcode** (App Store에서 설치)
   ```bash
   # 커맨드라인 도구 설정
   sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer
   sudo xcodebuild -runFirstLaunch

   # 라이선스 동의
   sudo xcodebuild -license accept
   ```

3. **CocoaPods**
   ```bash
   sudo gem install cocoapods
   ```

4. **Flutter 환경 확인**
   ```bash
   flutter doctor

   # 모든 항목이 ✓ 표시되어야 함
   # [✓] Flutter
   # [✓] Xcode
   # [✓] iOS Simulator
   ```

#### iOS 앱 실행 방법

##### 1. 의존성 설치

```bash
cd mobile

# Flutter 패키지 설치
flutter pub get

# iOS CocoaPods 설치
cd ios
pod install
cd ..
```

##### 2. iOS 시뮬레이터 확인

```bash
# 사용 가능한 시뮬레이터 목록
xcrun simctl list devices

# 예시 출력:
# iPhone 15 Pro (2B41DB31-47CF-44F1-BA6F-C4E846F87D00) (Shutdown)
# iPhone 15 (C1F5A9FC-590C-4093-B235-3E8E8C20CBF8) (Shutdown)
```

##### 3. 앱 실행

**방법 1: 기본 URL로 실행 (배포된 Frontend 사용)**

```bash
flutter run -d "iPhone 15 Pro"

# 자동으로 시뮬레이터 부팅 및 앱 설치
# URL: https://roulette-vibe.vercel.app
```

**방법 2: 로컬 Frontend 연결**

```bash
# Terminal 1: Frontend 실행
cd frontend
npm run dev
# → http://localhost:5173

# Terminal 2: Mobile 앱 실행 (로컬 연결)
cd mobile
flutter run -d "iPhone 15 Pro" --dart-define=WEB_APP_URL=http://localhost:5173
```

##### 4. 실행 확인

시뮬레이터에서 다음을 확인:

- ✅ **로딩**: Purple 스피너 표시
- ✅ **로그인 화면**: 닉네임 입력 필드
- ✅ **룰렛 페이지**: Purple-Pink 그라디언트, 룰렛 이미지
- ✅ **하단 탭**: 룰렛/포인트/상품/주문

#### iOS 앱 스펙

| 항목 | 내용 |
|------|------|
| **최소 iOS 버전** | iOS 11.0+ |
| **지원 기기** | iPhone, iPad |
| **화면 방향** | 세로 모드 고정 (Portrait) |
| **WebView 엔진** | WKWebView (iOS 기본) |
| **쿠키 저장** | 자동 (로그인 상태 유지) |
| **뒤로가기** | WebView 히스토리 → 앱 종료 |
| **JavaScript** | 활성화 (필수) |

#### 주요 기능

1. **WebView 렌더링**
   - Frontend 웹 앱을 네이티브처럼 렌더링
   - 로딩 상태 표시 (Purple 스피너)
   - 에러 시 재시도 버튼

2. **네비게이션**
   - 뒤로가기: WebView 히스토리 확인 → `goBack()` or 앱 종료
   - 세로 모드 고정 (회전 잠금)

3. **세션 유지**
   - 쿠키 자동 저장 → 로그인 상태 유지
   - 앱 종료 후 재실행해도 로그인 유지

4. **Hot Reload**
   ```bash
   # 터미널에서 입력
   r   # Hot reload (빠른 새로고침)
   R   # Hot restart (전체 재시작)
   q   # 앱 종료
   ```

#### 실제 iOS 기기 테스트

```bash
# iPhone USB 연결 후
flutter run

# 자동으로 연결된 기기 감지 및 설치
# (Apple Developer 계정 및 Xcode 서명 설정 필요)
```

#### 문제 해결

##### CocoaPods 오류
```bash
cd mobile/ios
pod install --repo-update
cd ..
```

##### 빌드 오류
```bash
flutter clean
flutter pub get
flutter run
```

##### 시뮬레이터 부팅 실패
```bash
# 시뮬레이터 수동 부팅
xcrun simctl boot "iPhone 15 Pro"

# Simulator 앱 열기
open -a Simulator

# 다시 실행
flutter run
```

---

### Android 앱 실행

#### 사전 요구사항

1. **Flutter SDK** (위 iOS 섹션 참고)
2. **Android Studio**
   - https://developer.android.com/studio 에서 다운로드
   - Android SDK 자동 설치

3. **환경변수 설정** (macOS)
   ```bash
   # ~/.zshrc 또는 ~/.bash_profile에 추가
   export ANDROID_HOME=$HOME/Library/Android/sdk
   export PATH=$PATH:$ANDROID_HOME/emulator
   export PATH=$PATH:$ANDROID_HOME/platform-tools

   # 적용
   source ~/.zshrc
   ```

4. **Flutter 환경 확인**
   ```bash
   flutter doctor

   # [✓] Android toolchain 확인
   ```

#### Android 앱 실행 방법

##### 1. 에뮬레이터 생성

```bash
# Android Studio 실행
# Tools > Device Manager > Create Device
# Phone > Pixel 6 > Next
# System Image: API 34 (UpsideDownCake) > Download > Finish
```

##### 2. 에뮬레이터 시작

```bash
# 에뮬레이터 목록 확인
flutter emulators

# 에뮬레이터 시작
flutter emulators --launch <emulator_id>

# 또는 Android Studio에서 수동 시작
```

##### 3. 앱 실행

```bash
cd mobile

# 자동으로 실행 중인 에뮬레이터 감지
flutter run

# 또는 특정 에뮬레이터 지정
flutter run -d emulator-5554
```

#### Android 앱 스펙

| 항목 | 내용 |
|------|------|
| **최소 Android 버전** | Android 5.0 (API 21+) |
| **지원 아키텍처** | arm64-v8a, armeabi-v7a, x86_64 |
| **WebView 엔진** | Android System WebView |
| **권한** | INTERNET, ACCESS_NETWORK_STATE |

---

## 배포

### 백엔드
- **플랫폼**: Render (Free Tier)
- **URL**: https://roulette-backend-upmn.onrender.com
- **CI/CD**: GitHub Actions (`.github/workflows/backend-deploy.yml`)

### 프론트엔드
- **플랫폼**: Vercel
- **사용자 웹**: https://roulette-vibe.vercel.app
- **어드민 웹**: https://roulette-admin.vercel.app
- **자동 배포**: `main` 브랜치 push 시

### 모바일
- **iOS**: Apple Developer 계정 필요 (App Store Connect)
- **Android**: Google Play Console 계정 필요

---

## 문서

| 문서 | 내용 |
|------|------|
| **CLAUDE.md** | 프로젝트 규칙, 정책, 코딩 컨벤션 |
| **docs/SPEC.md** | 백엔드 기술 명세서 |
| **docs/ADMIN_SPEC.md** | 어드민 웹 명세서 |
| **docs/HANDOFF.md** | 작업 인계 문서 |
| **backend/README.md** | 백엔드 가이드 |
| **frontend/README.md** | 사용자 웹 가이드 |
| **admin/README.md** | 어드민 웹 가이드 |
| **mobile/README.md** | Flutter 앱 가이드 |

---

## 라이선스

이 프로젝트는 교육 및 포트폴리오 목적으로 제작되었습니다.

---

## 기여

문제 발견 시 [Issues](../../issues)에 제보해주세요.

---

**Made with ❤️ by Claude & Team**