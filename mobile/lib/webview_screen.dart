import 'package:flutter/material.dart';
import 'package:webview_flutter/webview_flutter.dart';

class WebViewScreen extends StatefulWidget {
  final String url;

  const WebViewScreen({super.key, required this.url});

  @override
  State<WebViewScreen> createState() => _WebViewScreenState();
}

class _WebViewScreenState extends State<WebViewScreen> {
  late final WebViewController _controller;
  bool _isLoading = true;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _initializeWebView();
  }

  void _initializeWebView() {
    _controller = WebViewController()
      ..setJavaScriptMode(JavaScriptMode.unrestricted)
      ..setBackgroundColor(const Color(0x00000000))
      ..addJavaScriptChannel(
        'FlutterConsole',
        onMessageReceived: (JavaScriptMessage message) {
          // WebView 콘솔 로그를 Flutter 터미널에 출력
          print('[WebView Console] ${message.message}');
        },
      )
      ..setNavigationDelegate(
        NavigationDelegate(
          onPageStarted: (String url) {
            setState(() {
              _isLoading = true;
              _errorMessage = null;
            });
          },
          onPageFinished: (String url) {
            setState(() {
              _isLoading = false;
            });

            // JavaScript 콘솔 로그를 Flutter로 전달
            _controller.runJavaScript('''
              (function() {
                // 기존 console 메서드 오버라이드
                const originalLog = console.log;
                const originalError = console.error;
                const originalWarn = console.warn;

                console.log = function(...args) {
                  originalLog.apply(console, args);
                  window.FlutterConsole.postMessage('[LOG] ' + args.join(' '));
                };

                console.error = function(...args) {
                  originalError.apply(console, args);
                  window.FlutterConsole.postMessage('[ERROR] ' + args.join(' '));
                };

                console.warn = function(...args) {
                  originalWarn.apply(console, args);
                  window.FlutterConsole.postMessage('[WARN] ' + args.join(' '));
                };

                // Fetch API 에러 감지
                const originalFetch = window.fetch;
                window.fetch = function(...args) {
                  window.FlutterConsole.postMessage('[FETCH] ' + args[0]);
                  return originalFetch.apply(this, args)
                    .then(response => {
                      window.FlutterConsole.postMessage('[FETCH SUCCESS] ' + args[0] + ' - ' + response.status);
                      return response;
                    })
                    .catch(error => {
                      window.FlutterConsole.postMessage('[FETCH ERROR] ' + args[0] + ' - ' + error.message);
                      throw error;
                    });
                };

                window.FlutterConsole.postMessage('[WebView Ready] ' + window.location.href);
              })();
            ''');
          },
          onWebResourceError: (WebResourceError error) {
            setState(() {
              _isLoading = false;
              _errorMessage = error.description;
            });
          },
          onNavigationRequest: (NavigationRequest request) {
            // 외부 링크는 현재 WebView에서 허용
            return NavigationDecision.navigate;
          },
        ),
      )
      ..loadRequest(Uri.parse(widget.url));
  }

  Future<bool> _handleBackButton() async {
    final canGoBack = await _controller.canGoBack();
    if (canGoBack) {
      await _controller.goBack();
      return false; // 앱 종료 방지
    }
    return true; // 앱 종료 허용
  }

  void _reload() {
    _controller.reload();
    setState(() {
      _errorMessage = null;
    });
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (bool didPop, dynamic result) async {
        if (didPop) {
          return;
        }
        final shouldPop = await _handleBackButton();
        if (shouldPop && context.mounted) {
          Navigator.of(context).pop();
        }
      },
      child: Scaffold(
        body: SafeArea(
          child: Stack(
            children: [
              // WebView
              if (_errorMessage == null)
                WebViewWidget(controller: _controller)
              else
                _buildErrorView(),

              // 로딩 인디케이터
              if (_isLoading)
                const Center(
                  child: CircularProgressIndicator(
                    valueColor: AlwaysStoppedAnimation<Color>(
                      Color(0xFF9333EA), // Purple-600
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildErrorView() {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(
              Icons.error_outline,
              size: 64,
              color: Colors.red,
            ),
            const SizedBox(height: 16),
            const Text(
              '페이지를 불러올 수 없습니다',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              _errorMessage ?? '알 수 없는 오류',
              textAlign: TextAlign.center,
              style: const TextStyle(
                fontSize: 14,
                color: Colors.grey,
              ),
            ),
            const SizedBox(height: 24),
            ElevatedButton.icon(
              onPressed: _reload,
              icon: const Icon(Icons.refresh),
              label: const Text('다시 시도'),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF9333EA), // Purple-600
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(
                  horizontal: 24,
                  vertical: 12,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
