import 'package:flutter/material.dart';
import 'package:webview_flutter/webview_flutter.dart';

class WebViewScreen extends StatefulWidget {
  final String url;

  const WebViewScreen({super.key, required this.url});

  @override
  State<WebViewScreen> createState() => _WebViewScreenState();
}

class _WebViewScreenState extends State<WebViewScreen> {
  WebViewController? _controller;
  bool _isLoading = true;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _initializeWebView();
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
  }

  void _initializeWebView() async {
    // iOS WebView 쿠키 활성화
    final cookieManager = WebViewCookieManager();
    await cookieManager.clearCookies(); // 기존 쿠키 클리어

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
            print('🌐 [WebView] Page started: $url');
            setState(() {
              _isLoading = true;
              _errorMessage = null;
            });
          },
          onPageFinished: (String url) {
            print('✅ [WebView] Page finished: $url');
            setState(() {
              _isLoading = false;
            });

            // JavaScript 콘솔 로그를 Flutter로 전달
            print('📝 [WebView] Injecting console logger...');
            _controller?.runJavaScript('''
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

                // XMLHttpRequest 인터셉터 (axios용 - 중요!)
                const originalXHROpen = XMLHttpRequest.prototype.open;
                const originalXHRSend = XMLHttpRequest.prototype.send;

                const originalSetRequestHeader = XMLHttpRequest.prototype.setRequestHeader;

                XMLHttpRequest.prototype.open = function(method, url) {
                  this._url = url;
                  this._method = method;
                  window.FlutterConsole.postMessage('[XHR] ' + method + ' ' + url);
                  return originalXHROpen.apply(this, arguments);
                };

                XMLHttpRequest.prototype.setRequestHeader = function(header, value) {
                  return originalSetRequestHeader.call(this, header, value);
                };

                // 모든 요청에 세션 ID를 커스텀 헤더로 추가
                const addSessionHeader = function(xhr) {
                  const sessionId = localStorage.getItem('SESSION_ID');
                  if (sessionId) {
                    xhr.setRequestHeader('X-Session-ID', sessionId);
                    window.FlutterConsole.postMessage('[ADDING SESSION] X-Session-ID=' + sessionId.substring(0, 10) + '...');
                  }
                };

                XMLHttpRequest.prototype.send = function(body) {
                  const xhr = this;

                  // 요청 전에 세션 ID 헤더 추가 (로그인 제외)
                  if (!this._url.includes('/login')) {
                    addSessionHeader(this);
                  }

                  this.addEventListener('load', function() {
                    window.FlutterConsole.postMessage('[XHR SUCCESS] ' + this._method + ' ' + this._url + ' - ' + this.status);
                    window.FlutterConsole.postMessage('[XHR RESPONSE] ' + this.responseText.substring(0, 200));

                    // 로그인 성공 시 응답 본문에서 세션 ID 추출
                    if (this._url.includes('/login') && this.status === 200) {
                      try {
                        const response = JSON.parse(this.responseText);
                        if (response.success && response.data && response.data.sessionId) {
                          const sessionId = response.data.sessionId;
                          localStorage.setItem('SESSION_ID', sessionId);
                          window.FlutterConsole.postMessage('[SESSION SAVED FROM RESPONSE] ' + sessionId.substring(0, 10) + '...');
                        } else {
                          window.FlutterConsole.postMessage('[NO SESSION IN RESPONSE] ' + this.responseText.substring(0, 100));
                        }
                      } catch (e) {
                        window.FlutterConsole.postMessage('[SESSION PARSE ERROR] ' + e.message);
                      }
                    }
                  });
                  this.addEventListener('error', function() {
                    window.FlutterConsole.postMessage('[XHR ERROR] ' + this._method + ' ' + this._url);
                  });
                  return originalXHRSend.apply(this, arguments);
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

                // axios가 로드되면 인터셉터 추가
                const checkAxios = setInterval(function() {
                  if (window.axios) {
                    window.FlutterConsole.postMessage('[AXIOS FOUND] Adding interceptor');
                    window.axios.interceptors.request.use(function(config) {
                      const sessionId = localStorage.getItem('SESSION_ID');
                      if (sessionId && !config.url.includes('/login')) {
                        config.headers['X-Session-ID'] = sessionId;
                        window.FlutterConsole.postMessage('[AXIOS REQUEST] Adding session: ' + sessionId.substring(0, 10) + '...');
                      }
                      return config;
                    });

                    // 로그인 응답 인터셉터 - 응답에서 세션 ID 추출
                    window.axios.interceptors.response.use(function(response) {
                      if (response.config.url.includes('/login') && response.data.success && response.data.data.sessionId) {
                        const sessionId = response.data.data.sessionId;
                        localStorage.setItem('SESSION_ID', sessionId);
                        window.FlutterConsole.postMessage('[AXIOS SESSION SAVED] ' + sessionId.substring(0, 10) + '...');
                      }
                      return response;
                    });
                    clearInterval(checkAxios);
                  }
                }, 100);

                // 10초 후 타임아웃
                setTimeout(function() { clearInterval(checkAxios); }, 10000);

                window.FlutterConsole.postMessage('[WebView Ready] ' + window.location.href);
              })();
            ''');
          },
          onWebResourceError: (WebResourceError error) {
            print('❌ [WebView] Error: ${error.description}');
            print('   Error code: ${error.errorCode}');
            print('   Error type: ${error.errorType}');
            print('   URL: ${error.url}');
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

    print('🚀 [WebView] Loading URL: ${widget.url}');
  }

  Future<bool> _handleBackButton() async {
    if (_controller == null) return true;
    final canGoBack = await _controller!.canGoBack();
    if (canGoBack) {
      await _controller!.goBack();
      return false; // 앱 종료 방지
    }
    return true; // 앱 종료 허용
  }

  void _reload() {
    _controller?.reload();
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
              if (_errorMessage == null && _controller != null)
                WebViewWidget(controller: _controller!)
              else if (_errorMessage != null)
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
