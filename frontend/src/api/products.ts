import { apiClient } from './client';
import type { ApiResponse } from './client';

// ============================================
// 🎯 Advanced Type Definitions
// ============================================

/**
 * Branded Type: Product ID
 * - 런타임에는 number이지만, 타입 시스템에서는 구분됨
 */
type ProductId = number & { readonly __brand: 'ProductId' };

/**
 * Branded Type: Point Amount
 * - 포인트 금액을 명시적으로 구분
 */
type PointAmount = number & { readonly __brand: 'PointAmount' };

/**
 * Utility Type: Product Status (Computed)
 * - 구매 가능 여부를 타입으로 표현
 */
export type ProductAvailability =
  | { canPurchase: true; reason: null }
  | { canPurchase: false; reason: 'OUT_OF_STOCK' | 'INSUFFICIENT_POINTS' };

// ============================================
// 🔷 Core Domain Types
// ============================================

/**
 * 상품 항목
 */
export interface ProductItem {
  readonly id: ProductId;
  readonly name: string;
  readonly description: string | null;
  readonly price: PointAmount;
  readonly stock: number;
  readonly imageUrl: string | null;
  readonly isActive: boolean;
}

/**
 * 상품 상세 정보
 */
export interface ProductDetail extends ProductItem {
  readonly createdAt: string;
  readonly updatedAt: string;
}

/**
 * Type Guard: 재고가 있는 상품
 */
export function hasStock(product: ProductItem): product is ProductItem & { stock: number } {
  return product.stock > 0;
}

/**
 * Type Guard: 활성화된 상품
 */
export function isActiveProduct(product: ProductItem): product is ProductItem & { isActive: true } {
  return product.isActive;
}

/**
 * Type Predicate: 구매 가능한 상품 (재고 있고 활성화됨)
 */
export function isPurchasableProduct(product: ProductItem): product is ProductItem & { stock: number; isActive: true } {
  return hasStock(product) && isActiveProduct(product);
}

// ============================================
// 🔷 API Response Types
// ============================================

/**
 * 상품 목록 조회 응답
 */
export interface ProductListResponse {
  readonly products: readonly ProductItem[];
}

/**
 * 상품 상세 조회 응답
 */
export interface ProductDetailResponse extends ProductDetail {}

/**
 * 주문 생성 요청
 */
export interface CreateOrderRequest {
  readonly productId: ProductId;
}

/**
 * 주문 생성 응답
 */
export interface CreateOrderResponse {
  readonly orderId: number;
  readonly productName: string;
  readonly totalPrice: PointAmount;
  readonly remainingBalance: PointAmount;
}

/**
 * Mapped Type: Product with Purchase Info
 * - 구매 가능 여부 정보를 포함한 상품
 */
export type ProductWithPurchaseInfo = ProductItem & {
  readonly canPurchase: boolean;
  readonly insufficientPoints: boolean;
};

// ============================================
// 🚀 API Functions
// ============================================

/**
 * 상품 목록 조회
 * - 활성화된 상품만 반환 (isActive = true)
 * - 재고가 있는 상품만 반환 (stock > 0)
 */
export async function getProducts(): Promise<ApiResponse<ProductListResponse>> {
  const response = await apiClient.get<ApiResponse<ProductListResponse>>(
    '/api/user/products'
  );
  return response.data;
}

/**
 * 상품 상세 조회
 */
export async function getProduct(productId: ProductId): Promise<ApiResponse<ProductDetailResponse>> {
  const response = await apiClient.get<ApiResponse<ProductDetailResponse>>(
    `/api/user/products/${productId}`
  );
  return response.data;
}

/**
 * 상품 주문
 */
export async function createOrder(productId: number): Promise<ApiResponse<CreateOrderResponse>> {
  const response = await apiClient.post<ApiResponse<CreateOrderResponse>>(
    '/api/user/orders',
    { productId }
  );
  return response.data;
}

// ============================================
// 🔧 Utility Functions
// ============================================

/**
 * 구매 가능 여부 확인
 */
export function checkPurchaseAvailability(
  product: ProductItem,
  userBalance: number
): ProductAvailability {
  if (!hasStock(product)) {
    return { canPurchase: false, reason: 'OUT_OF_STOCK' };
  }

  if (userBalance < product.price) {
    return { canPurchase: false, reason: 'INSUFFICIENT_POINTS' };
  }

  return { canPurchase: true, reason: null };
}

/**
 * 상품에 구매 정보 추가
 */
export function withPurchaseInfo(
  product: ProductItem,
  userBalance: number
): ProductWithPurchaseInfo {
  const availability = checkPurchaseAvailability(product, userBalance);

  return {
    ...product,
    canPurchase: availability.canPurchase,
    insufficientPoints: availability.reason === 'INSUFFICIENT_POINTS',
  };
}

/**
 * 상품 목록 필터링: 구매 가능한 상품만
 */
export function filterPurchasableProducts<T extends ProductItem>(products: readonly T[]): T[] {
  return products.filter(isPurchasableProduct) as T[];
}

/**
 * 상품 목록 정렬: 가격 오름차순
 */
export function sortByPriceAsc<T extends ProductItem>(products: readonly T[]): T[] {
  return [...products].sort((a, b) => a.price - b.price);
}

/**
 * 상품 목록 정렬: 가격 내림차순
 */
export function sortByPriceDesc<T extends ProductItem>(products: readonly T[]): T[] {
  return [...products].sort((a, b) => b.price - a.price);
}

/**
 * 상품 목록 정렬: 재고 많은 순
 */
export function sortByStockDesc<T extends ProductItem>(products: readonly T[]): T[] {
  return [...products].sort((a, b) => b.stock - a.stock);
}
