import { apiClient } from './client';
import type { ApiResponse } from './client';

export interface DashboardData {
  budgetDate: string; // YYYY-MM-DD
  dailyLimit: number;
  remaining: number;
  usedAmount: number;
  participantCount: number;
}

export async function getDashboard(): Promise<ApiResponse<DashboardData>> {
  const response = await apiClient.get<ApiResponse<DashboardData>>('/api/admin/dashboard');
  return response.data;
}
