import { apiClient } from './client';
import type { ApiResponse } from './client';

// ============================================
// 🎯 Advanced Type Definitions
// ============================================

/**
 * Branded Type: Order ID
 */
type OrderId = number & { readonly __brand: 'OrderId' };

/**
 * Branded Type: ISO DateTime String
 */
type ISODateTimeString = string & { readonly __brand: 'ISODateTime' };

/**
 * Discriminated Union: Order Status
 * - COMPLETED: 주문 완료
 * - CANCELLED: 주문 취소
 */
type OrderStatus = 'COMPLETED' | 'CANCELLED';

/**
 * Utility Type: Order Status Info
 * - 주문 상태에 따른 정보를 타입으로 표현
 */
export type OrderStatusInfo =
  | { status: 'COMPLETED'; canCancel: false; isCancelled: false }
  | { status: 'CANCELLED'; canCancel: false; isCancelled: true };

// ============================================
// 🔷 Core Domain Types
// ============================================

/**
 * 주문 항목
 */
export interface OrderItem {
  readonly id: OrderId;
  readonly productName: string;
  readonly totalPrice: number;
  readonly status: OrderStatus;
  readonly createdAt: ISODateTimeString;
  readonly cancelledAt: ISODateTimeString | null;
}

/**
 * Type Guard: 취소된 주문
 */
export function isCancelledOrder(order: OrderItem): order is OrderItem & { status: 'CANCELLED'; cancelledAt: string } {
  return order.status === 'CANCELLED';
}

/**
 * Type Guard: 완료된 주문
 */
export function isCompletedOrder(order: OrderItem): order is OrderItem & { status: 'COMPLETED'; cancelledAt: null } {
  return order.status === 'COMPLETED';
}

/**
 * Type Predicate: 취소 가능한 주문 (사용자는 취소 불가, 어드민만 가능)
 * - 사용자 페이지에서는 항상 false
 */
export function canCancelOrder(_order: OrderItem): boolean {
  // PDP: 사용자는 주문 취소 불가
  return false;
}

// ============================================
// 🔷 API Response Types
// ============================================

/**
 * 주문 목록 조회 응답
 */
export interface OrderListResponse {
  readonly orders: readonly OrderItem[];
  readonly page: number;
  readonly size: number;
  readonly totalElements: number;
}

/**
 * Mapped Type: Order List with Navigation
 * - 페이지네이션 정보에 hasNext, hasPrevious 추가
 */
export type OrderListWithNavigation = OrderListResponse & {
  readonly totalPages: number;
  readonly hasNext: boolean;
  readonly hasPrevious: boolean;
};

// ============================================
// 🚀 API Functions
// ============================================

/**
 * 주문 내역 조회 (페이지네이션)
 * - 최신순 정렬 (createdAt DESC)
 */
export async function getOrders(page: number = 0, size: number = 20): Promise<ApiResponse<OrderListResponse>> {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
  });

  const response = await apiClient.get<ApiResponse<OrderListResponse>>(
    `/api/user/orders?${params.toString()}`
  );

  return response.data;
}

// ============================================
// 🔧 Utility Functions
// ============================================

/**
 * 주문 목록에 네비게이션 정보 추가
 */
export function withNavigation(response: OrderListResponse): OrderListWithNavigation {
  const { page, size, totalElements } = response;
  const totalPages = totalElements === 0 ? 1 : Math.ceil(totalElements / size);

  return {
    ...response,
    totalPages,
    hasNext: page < totalPages - 1,
    hasPrevious: page > 0,
  };
}

/**
 * 주문 상태별 라벨 반환
 */
export function getOrderStatusLabel(status: OrderStatus): string {
  const labels: Record<OrderStatus, string> = {
    COMPLETED: '주문 완료',
    CANCELLED: '주문 취소',
  };
  return labels[status];
}

/**
 * 주문 상태별 색상 클래스 반환
 */
export function getOrderStatusColor(status: OrderStatus): string {
  const colors: Record<OrderStatus, string> = {
    COMPLETED: 'text-green-600 bg-green-50 border-green-300',
    CANCELLED: 'text-gray-600 bg-gray-50 border-gray-300',
  };
  return colors[status];
}

/**
 * 주문 상태 정보 생성
 */
export function getOrderStatusInfo(order: OrderItem): OrderStatusInfo {
  if (isCancelledOrder(order)) {
    return { status: 'CANCELLED', canCancel: false, isCancelled: true };
  }
  return { status: 'COMPLETED', canCancel: false, isCancelled: false };
}

/**
 * 주문 목록 필터링: 완료된 주문만
 */
export function filterCompletedOrders<T extends OrderItem>(orders: readonly T[]): T[] {
  return orders.filter(isCompletedOrder) as T[];
}

/**
 * 주문 목록 필터링: 취소된 주문만
 */
export function filterCancelledOrders<T extends OrderItem>(orders: readonly T[]): T[] {
  return orders.filter(isCancelledOrder) as T[];
}

/**
 * 주문 목록 정렬: 최신순
 */
export function sortByNewest<T extends OrderItem>(orders: readonly T[]): T[] {
  return [...orders].sort((a, b) =>
    new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  );
}

/**
 * 주문 목록 정렬: 오래된 순
 */
export function sortByOldest<T extends OrderItem>(orders: readonly T[]): T[] {
  return [...orders].sort((a, b) =>
    new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
  );
}
