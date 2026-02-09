import { apiClient, type ApiResponse } from './client';

export interface SpinResponse {
  historyId: number;
  amount: number;
  actualGrantedAmount: number;
  recoveredAmount: number;
  remainingBudget: number;
  message: string;
}

export interface BudgetResponse {
  date: string; // YYYY-MM-DD
  limitAmount: number;
  remaining: number;
  used: number;
}

export interface RouletteHistoryDto {
  historyId: number;
  amount: number;
  spinDate: string; // YYYY-MM-DD
}

export interface RouletteStatusResponse {
  participated: boolean;
  remainingBudget: number;
  history: RouletteHistoryDto | null;
}

export async function spin(): Promise<SpinResponse> {
  const response = await apiClient.post<ApiResponse<SpinResponse>>(
    '/api/user/roulette/spin'
  );
  return response.data.data;
}

export async function getBudget(): Promise<BudgetResponse> {
  const response = await apiClient.get<ApiResponse<BudgetResponse>>(
    '/api/user/roulette/budget'
  );
  return response.data.data;
}

export async function getStatus(): Promise<RouletteStatusResponse> {
  const response = await apiClient.get<ApiResponse<RouletteStatusResponse>>(
    '/api/user/roulette/status'
  );
  return response.data.data;
}
