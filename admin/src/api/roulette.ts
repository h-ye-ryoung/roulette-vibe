import { apiClient } from './client';
import type { ApiResponse } from './client';

export interface RouletteHistory {
  historyId: number;
  userName: string;
  originalAmount: number;
  reclaimedAmount: number;
  spinDate: string; // YYYY-MM-DD
  status: 'ACTIVE' | 'CANCELLED';
}

export interface RouletteHistoryPage {
  items: RouletteHistory[];
  pageInfo: {
    currentPage: number;
    totalPages: number;
    pageSize: number;
    totalElements: number;
    hasNext: boolean;
    hasPrevious: boolean;
  };
}

export interface RouletteHistoryParams {
  page?: number;
  size?: number;
  date?: string; // YYYY-MM-DD
}

export async function getRouletteHistory(
  params: RouletteHistoryParams = {}
): Promise<ApiResponse<RouletteHistoryPage>> {
  const { page = 0, size = 20, date } = params;
  const queryParams = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
  });

  if (date) {
    queryParams.append('date', date);
  }

  const response = await apiClient.get<ApiResponse<RouletteHistoryPage>>(
    `/api/admin/roulette/history?${queryParams.toString()}`
  );
  return response.data;
}

export async function cancelRoulette(historyId: number): Promise<ApiResponse<void>> {
  const response = await apiClient.post<ApiResponse<void>>(
    `/api/admin/roulette/${historyId}/cancel`
  );
  return response.data;
}
