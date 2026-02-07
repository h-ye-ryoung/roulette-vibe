import { apiClient } from './client';

export interface Product {
  id: number;
  name: string;
  description: string | null;
  price: number;
  stock: number;
  imageUrl?: string | null;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductFormData {
  name: string;
  description: string;
  price: number;
  stock: number;
  isActive: boolean;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error?: {
    code: string;
    message: string;
  };
}

export interface ProductListResponse {
  products: Product[];
}

/**
 * 상품 목록 조회
 */
export async function getProducts(): Promise<Product[]> {
  const response = await apiClient.get<ApiResponse<ProductListResponse>>('/api/admin/products');
  return response.data.data.products;
}

/**
 * 상품 생성
 */
export async function createProduct(data: ProductFormData): Promise<Product> {
  const response = await apiClient.post<ApiResponse<Product>>('/api/admin/products', data);
  return response.data.data;
}

/**
 * 상품 수정
 */
export async function updateProduct(id: number, data: ProductFormData): Promise<Product> {
  const response = await apiClient.put<ApiResponse<Product>>(`/api/admin/products/${id}`, data);
  return response.data.data;
}

/**
 * 상품 삭제
 */
export async function deleteProduct(id: number): Promise<void> {
  await apiClient.delete<ApiResponse<void>>(`/api/admin/products/${id}`);
}
