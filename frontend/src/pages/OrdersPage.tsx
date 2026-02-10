import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';
import { ko } from 'date-fns/locale';
import { useAuth } from '@/contexts/AuthContext';
import {
  getOrders,
  withNavigation,
  getOrderStatusLabel,
  getOrderStatusColor,
  type OrderItem,
} from '@/api/orders';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { ShoppingBag, Package } from 'lucide-react';
import AppLayout from '@/components/layout/AppLayout';
import { FullScreenLoading } from '@/components/LoadingSpinner';

export default function OrdersPage() {
  const { user } = useAuth();
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 10;

  // 주문 내역 조회
  const { data: ordersData, isLoading } = useQuery({
    queryKey: ['orders', currentPage],
    queryFn: () => getOrders(currentPage, pageSize),
  });

  const ordersResponse = ordersData?.data;
  const orders = ordersResponse ? withNavigation(ordersResponse) : null;

  // 로딩 상태
  if (isLoading) {
    return <FullScreenLoading message="주문 내역을 불러오는 중..." />;
  }

  return (
    <AppLayout title="📦 주문 내역">
      <div className="space-y-6 pb-32">
        {/* 환영 메시지 */}
        <div className="text-center space-y-1">
          <p className="text-lg font-semibold text-gray-800">
            <span className="bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
              {user?.nickname}
            </span>
            님의 주문 내역 📦
          </p>
          <p className="text-sm text-gray-600">포인트로 구매한 상품 목록입니다</p>
        </div>

        {/* 주문 내역 카드 */}
        <Card className="backdrop-blur-lg bg-white/70 border-white/20 shadow-xl">
          <CardHeader>
            <CardTitle className="text-center text-lg">주문 목록</CardTitle>
          </CardHeader>
          <CardContent>
            {orders && orders.orders.length > 0 ? (
              <>
                <div className="space-y-3">
                  {orders.orders.map((order) => (
                    <OrderCard key={order.id} order={order} />
                  ))}
                </div>

                {/* 페이지네이션 */}
                {orders.totalPages > 1 && (
                  <div className="mt-4 pt-4 border-t flex items-center justify-between">
                    <p className="text-xs text-gray-500">
                      {currentPage + 1} / {orders.totalPages} 페이지
                      <span className="ml-1 text-gray-400">
                        (전체 {orders.totalElements}건)
                      </span>
                    </p>
                    <div className="flex gap-2">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setCurrentPage((p) => p - 1)}
                        disabled={!orders.hasPrevious}
                        className="h-8 px-3 text-xs"
                      >
                        이전
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setCurrentPage((p) => p + 1)}
                        disabled={!orders.hasNext}
                        className="h-8 px-3 text-xs"
                      >
                        다음
                      </Button>
                    </div>
                  </div>
                )}
              </>
            ) : (
              <div className="py-8 text-center space-y-2">
                <p className="text-4xl">🛍️</p>
                <p className="text-sm text-gray-600 font-medium">주문 내역이 없습니다</p>
                <p className="text-xs text-gray-500">상품을 구매하면 여기에 표시됩니다!</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  );
}

// ============================================
// 🎨 OrderCard 컴포넌트
// ============================================

interface OrderCardProps {
  order: OrderItem;
}

function OrderCard({ order }: OrderCardProps) {
  const isCancelled = order.status === 'CANCELLED';
  const statusLabel = getOrderStatusLabel(order.status);
  const statusColor = getOrderStatusColor(order.status);

  return (
    <div
      className={`
        p-4 rounded-lg border transition-all
        ${
          isCancelled
            ? 'bg-gray-50/50 border-gray-200 opacity-70'
            : 'bg-gradient-to-r from-purple-50/30 to-pink-50/30 border-purple-100/50 hover:shadow-md'
        }
      `}
    >
      <div className="flex items-center gap-4">
        {/* 상품 아이콘 */}
        <div
          className={`
            flex-shrink-0 w-12 h-12 rounded-lg flex items-center justify-center
            ${
              isCancelled
                ? 'bg-gray-100'
                : 'bg-gradient-to-br from-purple-100 to-pink-100'
            }
          `}
        >
          <Package className={`w-6 h-6 ${isCancelled ? 'text-gray-400' : 'text-purple-600'}`} />
        </div>

        {/* 주문 정보 */}
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-1">
            <h3 className={`text-base font-semibold truncate ${isCancelled ? 'text-gray-500' : 'text-gray-800'}`}>
              {order.productName}
            </h3>
          </div>

          <div className="flex items-center gap-2 text-xs text-gray-500">
            <ShoppingBag className="w-3 h-3" />
            <span>{format(new Date(order.createdAt), 'M월 d일 HH:mm', { locale: ko })}</span>
          </div>

          {order.cancelledAt && (
            <div className="mt-1 text-xs text-gray-400">
              취소: {format(new Date(order.cancelledAt), 'M월 d일 HH:mm', { locale: ko })}
            </div>
          )}
        </div>

        {/* 가격 및 상태 */}
        <div className="flex-shrink-0 flex flex-col items-end gap-2">
          <div className={`text-lg font-bold ${isCancelled ? 'text-gray-400' : 'text-purple-600'}`}>
            {order.totalPrice.toLocaleString()}p
          </div>
          <Badge variant="outline" className={`text-xs px-2 py-0.5 ${statusColor}`}>
            {statusLabel}
          </Badge>
        </div>
      </div>
    </div>
  );
}
