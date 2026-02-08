import { apiClient, type ApiResponse } from './client';

export interface LoginRequest {
  nickname: string;
}

export interface LoginResponse {
  id: number;
  nickname: string;
}

export async function login(nickname: string): Promise<LoginResponse> {
  const response = await apiClient.post<ApiResponse<LoginResponse>>(
    '/api/auth/login',
    { nickname }
  );
  return response.data.data;
}

export async function logout(): Promise<void> {
  await apiClient.post('/api/auth/logout');
}

export async function getCurrentUser(): Promise<LoginResponse> {
  const response = await apiClient.get<ApiResponse<LoginResponse>>('/api/auth/me');
  return response.data.data;
}
