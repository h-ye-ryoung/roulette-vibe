import axios from 'axios';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true, // 세션 쿠키 전송 (fallback)
  headers: {
    'Content-Type': 'application/json',
  },
});

// 요청 인터셉터: localStorage의 SESSION_ID를 X-Session-ID 헤더로 추가
apiClient.interceptors.request.use((config) => {
  const sessionId = localStorage.getItem('SESSION_ID');
  if (sessionId) {
    config.headers['X-Session-ID'] = sessionId;
  }
  return config;
});

// 응답 인터셉터: 로그인 응답에서 sessionId를 localStorage에 저장
apiClient.interceptors.response.use((response) => {
  if (response.config.url?.includes('/login') && response.data.success && response.data.data.sessionId) {
    const sessionId = response.data.data.sessionId;
    localStorage.setItem('SESSION_ID', sessionId);
    console.log('[Auth] Session ID saved:', sessionId);
  }
  return response;
});

// 공통 응답 타입
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error?: {
    code: string;
    message: string;
  };
}
