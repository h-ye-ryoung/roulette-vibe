export interface User {
  id: number;
  nickname: string;
  role: 'ADMIN' | 'USER';
}

export interface DashboardData {
  budgetDate: string; // YYYY-MM-DD
  dailyLimit: number;
  remaining: number;
  usedAmount: number;
  participantCount: number;
}

export interface BudgetData {
  dailyLimit: number;
}

export interface Product {
  id: number;
  name: string;
  price: number;
  stock: number;
  description: string;
  isActive: boolean;
}

export interface Order {
  id: number;
  userId: number;
  userNickname: string;
  productId: number;
  productName: string;
  productPrice: number;
  status: 'COMPLETED' | 'CANCELLED';
  createdAt: string;
}

export interface RouletteHistory {
  id: number;
  userId: number;
  userNickname: string;
  amount: number;
  balance: number;
  status: 'ACTIVE' | 'CANCELLED';
  spinDate: string; // YYYY-MM-DD
  createdAt: string;
}

export interface PageInfo {
  currentPage: number;
  totalPages: number;
  pageSize: number;
  totalElements: number;
  hasNext: boolean;
  hasPrevious: boolean;
}
