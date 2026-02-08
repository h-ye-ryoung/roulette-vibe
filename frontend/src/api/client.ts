import axios from 'axios';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true, // 세션 쿠키 전송 (사용자 인증 필요)
  headers: {
    'Content-Type': 'application/json',
  },
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
