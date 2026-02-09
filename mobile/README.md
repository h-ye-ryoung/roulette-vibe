# Point Roulette Mobile App

Flutter WebView 래퍼 앱 - iOS/Android 지원

## 개요

- Frontend 사용자 웹 앱을 WebView로 렌더링
- UI 재구현 없이 웹 화면 그대로 사용
- 로그인 상태 자동 유지 (쿠키 저장)
- 뒤로가기 처리 (WebView 히스토리 또는 앱 종료)

## 기술 스택

- Flutter SDK 3.10+
- Dart 3.10+
- webview_flutter: ^4.10.0
- url_launcher: ^6.3.1

## 프로젝트 구조

```
mobile/
├── lib/
│   ├── main.dart           # 앱 진입점
│   └── webview_screen.dart # WebView 화면
├── ios/
│   └── Runner/
│       └── Info.plist      # iOS 권한 설정
├── android/
│   └── app/src/main/
│       └── AndroidManifest.xml  # Android 권한 설정
└── pubspec.yaml            # 패키지 설정
```

## 실행 방법

### 1. 의존성 설치

```bash
cd mobile
flutter pub get
```

### 2. 로컬 개발 (배포된 프론트엔드 사용)

```bash
# 기본 URL (Vercel 배포)
flutter run

# 또는 특정 시뮬레이터/에뮬레이터 지정
flutter run -d "iPhone 15 Pro"
flutter run -d emulator-5554
```

### 3. 로컬 프론트엔드 연결

```bash
# 로컬 웹서버(http://localhost:5173) 연결
flutter run --dart-define=WEB_APP_URL=http://localhost:5173
```

## 빌드

### iOS (Release)

```bash
flutter build ios --release

# 실제 기기 빌드는 Apple Developer 계정 필요
```

### Android (APK)

```bash
flutter build apk --release

# 출력: build/app/outputs/flutter-apk/app-release.apk
```

## 주요 기능

### 1. WebView 렌더링
- JavaScript 활성화
- 쿠키 자동 저장 (로그인 상태 유지)
- 페이지 로딩 상태 표시

### 2. 뒤로가기 처리
- WebView 히스토리 있으면 goBack()
- 없으면 앱 종료

### 3. 에러 처리
- 네트워크 오류 시 에러 화면 표시
- 다시 시도 버튼 제공

### 4. 화면 방향 고정
- 세로 모드만 지원 (Portrait)

## 환경변수

### WEB_APP_URL
WebView로 로드할 웹 앱 URL

**기본값**: `https://roulette-frontend.vercel.app`

**사용 예시**:
```bash
# 로컬 개발
flutter run --dart-define=WEB_APP_URL=http://localhost:5173

# 프로덕션 (기본값)
flutter run
```

## 문제 해결

### CocoaPods 오류 (iOS)
```bash
cd ios
pod install --repo-update
cd ..
```

### Gradle 빌드 오류 (Android)
```bash
cd android
./gradlew clean
cd ..
flutter clean
flutter pub get
```

## 관련 문서

- [Frontend 웹 앱](../frontend/README.md)
- [백엔드 API](../backend/README.md)
- [프로젝트 명세](../docs/SPEC.md)
