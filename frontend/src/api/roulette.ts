import { apiClient, type ApiResponse } from './client';

export interface SpinResponse {
  amount: number;
  remainingBudget: number;
}

export interface BudgetResponse {
  date: string; // YYYY-MM-DD
  limitAmount: number;
  remaining: number;
  used: number;
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
