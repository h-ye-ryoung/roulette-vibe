import { apiClient } from './client';
import type { ApiResponse } from './client';

export interface BudgetData {
  budgetDate: string; // YYYY-MM-DD
  dailyLimit: number;
  remaining: number;
}

export interface UpdateBudgetRequest {
  dailyLimit: number;
}

export async function getBudget(): Promise<ApiResponse<BudgetData>> {
  const response = await apiClient.get<ApiResponse<BudgetData>>('/api/admin/budget');
  return response.data;
}

export async function updateBudget(data: UpdateBudgetRequest): Promise<ApiResponse<BudgetData>> {
  const response = await apiClient.put<ApiResponse<BudgetData>>('/api/admin/budget', data);
  return response.data;
}
