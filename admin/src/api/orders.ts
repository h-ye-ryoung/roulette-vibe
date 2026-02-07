import { apiClient } from './client';

export const OrderStatus = {
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
} as const;

export type OrderStatus = (typeof OrderStatus)[keyof typeof OrderStatus];

export interface Order {
  id: number;
  userId: number;
  userName: string;
  productId: number;
  productName: string;
  totalPrice: number;
  status: OrderStatus;
  createdAt: string;
  cancelledAt: string | null;
}

export interface OrderListResponse {
  orders: Order[];
  page: number;
  size: number;
  totalElements: number;
}

export interface CancelOrderResponse {
  orderId: number;
  userId: number;
  userName: string;
  refundedAmount: number;
  message: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error?: {
    code: string;
    message: string;
  };
}

/**
 * 주문 목록 조회
 */
export async function getOrders(
  page: number = 0,
  size: number = 20,
  status?: OrderStatus
): Promise<OrderListResponse> {
  const params: Record<string, string | number> = { page, size };
  if (status) {
    params.status = status;
  }

  const response = await apiClient.get<ApiResponse<OrderListResponse>>(
    '/api/admin/orders',
    { params }
  );
  return response.data.data;
}

/**
 * 주문 취소
 */
export async function cancelOrder(orderId: number): Promise<CancelOrderResponse> {
  const response = await apiClient.post<ApiResponse<CancelOrderResponse>>(
    `/api/admin/orders/${orderId}/cancel`
  );
  return response.data.data;
}
