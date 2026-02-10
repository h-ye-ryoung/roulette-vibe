import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'webview_screen.dart';

// WebView URL 환경변수 설정
const String kWebAppUrl = String.fromEnvironment(
  'WEB_APP_URL',
  defaultValue: 'https://roulette-vibe.vercel.app',
);

void main() {
  WidgetsFlutterBinding.ensureInitialized();

  // 세로 모드 고정
  SystemChrome.setPreferredOrientations([
    DeviceOrientation.portraitUp,
    DeviceOrientation.portraitDown,
  ]);

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: '포인트 룰렛',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF9333EA), // Purple-600
        ),
        useMaterial3: true,
      ),
      home: const WebViewScreen(url: kWebAppUrl),
    );
  }
}
