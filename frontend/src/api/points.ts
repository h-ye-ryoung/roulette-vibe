import { apiClient } from './client';
import type { ApiResponse } from './client';

// ============================================
// 🎯 Advanced Type Definitions
// ============================================

/**
 * Branded Type: ISO DateTime String
 * - 런타임에는 string이지만, 타입 시스템에서는 구분됨
 */
type ISODateTimeString = string & { readonly __brand: 'ISODateTime' };

/**
 * Discriminated Union: Point Type
 * - EARN: 룰렛으로 획득한 포인트 (증가, +)
 * - REFUND: 주문 취소로 환불받은 포인트 (증가, +)
 * - USED: 상품 구매로 사용한 포인트 (차감, -)
 * - RECLAIMED: 룰렛 취소로 회수된 포인트 (차감, -)
 */
type PointType = 'EARN' | 'REFUND' | 'USED' | 'RECLAIMED';

/**
 * Utility Type: Point Status (Computed)
 * - expired와 expiringSoon으로 상태를 구분
 * - Type demonstration purpose only
 */
// type PointStatus =
//   | { expired: true; expiringSoon: false }
//   | { expired: false; expiringSoon: true }
//   | { expired: false; expiringSoon: false };

/**
 * Conditional Type: Point Item with Status
 * - expired가 true면 balance는 항상 0
 * - Type demonstration purpose only
 */
// type PointItemWithStatus<T extends boolean> = T extends true
//   ? { balance: 0; expired: true }
//   : { balance: number; expired: false };

// ============================================
// 🔷 Core Domain Types
// ============================================

/**
 * 만료 예정 포인트 (7일 이내)
 */
export interface ExpiringPoint {
  readonly id: number;
  readonly balance: number;
  readonly expiresAt: ISODateTimeString;
}

/**
 * 포인트 내역 항목
 */
export interface PointItem {
  readonly id: number;
  readonly amount: number;
  readonly balance: number;
  readonly type: PointType;
  readonly issuedAt: ISODateTimeString;
  readonly expiresAt: ISODateTimeString;
  readonly expired: boolean;
  readonly expiringSoon: boolean;
}

/**
 * Type Guard: expired가 true인 PointItem
 */
export function isExpiredPoint(point: PointItem): point is PointItem & { expired: true; balance: 0 } {
  return point.expired;
}

/**
 * Type Guard: expiringSoon이 true인 PointItem
 */
export function isExpiringSoonPoint(point: PointItem): point is PointItem & { expiringSoon: true; expired: false } {
  return point.expiringSoon && !point.expired;
}

/**
 * Type Predicate: 유효한 포인트 (만료되지 않음)
 */
export function isValidPoint(point: PointItem): point is PointItem & { expired: false } {
  return !point.expired;
}

// ============================================
// 🔷 API Response Types
// ============================================

/**
 * 포인트 잔액 조회 응답
 */
export interface BalanceResponse {
  readonly totalBalance: number;
  readonly expiringPoints: readonly ExpiringPoint[];
}

/**
 * 만료 예정 포인트 조회 응답
 */
export interface ExpiringPointsResponse {
  readonly expiringPoints: readonly ExpiringPoint[];
  readonly totalExpiringBalance: number;
}

/**
 * 포인트 내역 조회 응답 (페이지네이션)
 */
export interface PointHistoryResponse {
  readonly items: readonly PointItem[];
  readonly totalCount: number;
  readonly currentPage: number;
  readonly totalPages: number;
}

/**
 * Utility Type: Paginated Response
 * - Generic으로 재사용 가능한 페이지네이션 타입
 */
export type PaginatedResponse<T> = {
  readonly items: readonly T[];
  readonly totalCount: number;
  readonly currentPage: number;
  readonly totalPages: number;
  readonly hasNext: boolean;
  readonly hasPrevious: boolean;
};

/**
 * Mapped Type: Point Item with Computed Fields
 * - 페이지네이션 정보에 hasNext, hasPrevious 추가
 */
export type PointHistoryWithNavigation = Omit<PointHistoryResponse, 'items'> & {
  readonly items: readonly PointItem[];
  readonly hasNext: boolean;
  readonly hasPrevious: boolean;
};

// ============================================
// 🔷 API Request Parameters
// ============================================

/**
 * 포인트 내역 조회 파라미터
 */
export interface PointHistoryParams {
  readonly page?: number;
  readonly size?: number;
}

/**
 * Partial Type: Optional Parameters
 * - 모든 파라미터를 선택적으로 만듦
 */
export type OptionalPointHistoryParams = Partial<PointHistoryParams>;

// ============================================
// 🚀 API Functions
// ============================================

/**
 * 포인트 잔액 조회
 * - 총 유효 포인트 잔액
 * - 7일 이내 만료 예정 포인트 목록
 */
export async function getBalance(): Promise<ApiResponse<BalanceResponse>> {
  const response = await apiClient.get<ApiResponse<BalanceResponse>>(
    '/api/user/points/balance'
  );
  return response.data;
}

/**
 * 만료 예정 포인트 조회
 * - 7일 이내 만료 예정인 포인트 목록
 * - 만료일 기준 오름차순 정렬
 */
export async function getExpiringPoints(): Promise<ApiResponse<ExpiringPointsResponse>> {
  const response = await apiClient.get<ApiResponse<ExpiringPointsResponse>>(
    '/api/user/points/expiring'
  );
  return response.data;
}

/**
 * 포인트 내역 조회 (페이지네이션)
 * - 유효/만료 포인트 모두 포함
 * - 최신순 정렬 (issuedAt DESC)
 */
export async function getPointHistory(
  params: OptionalPointHistoryParams = {}
): Promise<ApiResponse<PointHistoryResponse>> {
  const { page = 0, size = 20 } = params;

  const queryParams = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
  });

  const response = await apiClient.get<ApiResponse<PointHistoryResponse>>(
    `/api/user/points/history?${queryParams.toString()}`
  );

  return response.data;
}

// ============================================
// 🔧 Utility Functions
// ============================================

/**
 * 포인트 히스토리에 네비게이션 정보 추가
 * - hasNext, hasPrevious 계산
 */
export function withNavigation(response: PointHistoryResponse): PointHistoryWithNavigation {
  const { currentPage, totalPages } = response;

  return {
    ...response,
    hasNext: currentPage < totalPages - 1,
    hasPrevious: currentPage > 0,
  };
}

/**
 * 포인트 타입별 라벨 반환
 */
export function getPointTypeLabel(type: PointType): string {
  const labels: Record<PointType, string> = {
    EARN: '룰렛 참여',
    REFUND: '포인트 환불',
    USED: '상품 주문',
    RECLAIMED: '포인트 회수',
  };
  return labels[type];
}

/**
 * 포인트 타입별 색상 클래스 반환
 */
export function getPointTypeColor(type: PointType): string {
  const colors: Record<PointType, string> = {
    EARN: 'text-blue-600',       // 증가 (파랑)
    REFUND: 'text-blue-600',     // 증가 (파랑)
    USED: 'text-red-600',        // 차감 (빨강)
    RECLAIMED: 'text-red-600',   // 차감 (빨강)
  };
  return colors[type];
}

/**
 * 포인트 타입이 증가(+)인지 확인
 */
export function isIncreaseType(type: PointType): boolean {
  return type === 'EARN' || type === 'REFUND';
}

/**
 * 포인트 타입이 차감(-)인지 확인
 */
export function isDecreaseType(type: PointType): boolean {
  return type === 'USED' || type === 'RECLAIMED';
}

/**
 * 만료일까지 남은 일수 계산
 */
export function getDaysUntilExpiry(expiresAt: ISODateTimeString): number {
  const now = new Date();
  const expiry = new Date(expiresAt);
  const diffTime = expiry.getTime() - now.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  return Math.max(0, diffDays);
}

/**
 * 포인트 항목 필터링: 유효한 포인트만
 */
export function filterValidPoints<T extends PointItem>(points: readonly T[]): T[] {
  return points.filter(isValidPoint) as T[];
}

/**
 * 포인트 항목 필터링: 만료 예정 포인트만
 */
export function filterExpiringSoonPoints<T extends PointItem>(points: readonly T[]): T[] {
  return points.filter(isExpiringSoonPoint) as T[];
}

/**
 * 포인트 항목 필터링: 만료된 포인트만
 */
export function filterExpiredPoints<T extends PointItem>(points: readonly T[]): T[] {
  return points.filter(isExpiredPoint) as T[];
}
