import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { format } from 'date-fns';
import { XCircle } from 'lucide-react';
import {
  getOrders,
  cancelOrder,
  OrderStatus,
  type Order,
} from '@/api/orders';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Skeleton } from '@/components/ui/skeleton';

export default function OrdersPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<OrderStatus | 'ALL'>('ALL');
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [orderToCancel, setOrderToCancel] = useState<Order | null>(null);

  // 주문 목록 조회
  const { data, isLoading, error } = useQuery({
    queryKey: ['orders', page, statusFilter],
    queryFn: () =>
      getOrders(page, 20, statusFilter === 'ALL' ? undefined : statusFilter),
  });

  // 주문 취소 Mutation
  const cancelMutation = useMutation({
    mutationFn: cancelOrder,
    onSuccess: (response) => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      setCancelDialogOpen(false);
      setOrderToCancel(null);
      alert(response.message);
    },
    onError: (error: any) => {
      const message = error.response?.data?.error?.message || '주문 취소에 실패했습니다';
      const errorCode = error.response?.data?.error?.code;

      if (errorCode === 'ORDER_ALREADY_CANCELLED') {
        alert('이미 취소된 주문입니다');
      } else {
        alert(message);
      }
      setCancelDialogOpen(false);
      setOrderToCancel(null);
    },
  });

  // 취소 버튼 클릭
  const handleCancelClick = (order: Order) => {
    setOrderToCancel(order);
    setCancelDialogOpen(true);
  };

  // 취소 확인
  const handleCancelConfirm = () => {
    if (orderToCancel) {
      cancelMutation.mutate(orderToCancel.id);
    }
  };

  // 상태 필터 변경
  const handleStatusChange = (value: string) => {
    setStatusFilter(value as OrderStatus | 'ALL');
    setPage(0); // 필터 변경 시 첫 페이지로 이동
  };

  // 필터 초기화
  const handleResetFilter = () => {
    setStatusFilter('ALL');
    setPage(0);
  };

  // 날짜 포맷팅
  const formatDateTime = (dateString: string) => {
    try {
      return format(new Date(dateString), 'yyyy-MM-dd HH:mm');
    } catch {
      return dateString;
    }
  };

  // 페이지네이션 계산
  const totalPages = data ? Math.ceil(data.totalElements / 20) : 0;
  const hasNextPage = page < totalPages - 1;
  const hasPrevPage = page > 0;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">주문 관리</h1>
      </div>

      {/* 필터 영역 */}
      <Card>
        <CardHeader>
          <CardTitle>필터</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-end gap-4">
            <div className="flex-1 max-w-xs space-y-2">
              <label className="text-sm font-medium">주문 상태</label>
              <Select value={statusFilter} onValueChange={handleStatusChange}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">전체</SelectItem>
                  <SelectItem value={OrderStatus.COMPLETED}>COMPLETED</SelectItem>
                  <SelectItem value={OrderStatus.CANCELLED}>CANCELLED</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <Button variant="outline" onClick={handleResetFilter}>
              필터 초기화
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* 주문 목록 */}
      <Card>
        <CardHeader>
          <CardTitle>주문 목록</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading && (
            <div className="space-y-2">
              <Skeleton className="h-12 w-full" />
              <Skeleton className="h-12 w-full" />
              <Skeleton className="h-12 w-full" />
            </div>
          )}

          {error && (
            <div className="text-center py-8 text-red-500">
              주문 목록을 불러오는데 실패했습니다
            </div>
          )}

          {!isLoading && !error && data && data.orders.length === 0 && (
            <div className="text-center py-8 text-muted-foreground">
              주문 내역이 없습니다
            </div>
          )}

          {!isLoading && !error && data && data.orders.length > 0 && (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>주문번호</TableHead>
                    <TableHead>사용자명</TableHead>
                    <TableHead>상품명</TableHead>
                    <TableHead className="text-right">금액</TableHead>
                    <TableHead>상태</TableHead>
                    <TableHead>주문일시</TableHead>
                    <TableHead className="text-right">액션</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {data.orders.map((order) => (
                    <TableRow key={order.id}>
                      <TableCell className="font-medium">{order.id}</TableCell>
                      <TableCell>{order.userName}</TableCell>
                      <TableCell>{order.productName}</TableCell>
                      <TableCell className="text-right">
                        {order.totalPrice.toLocaleString()}p
                      </TableCell>
                      <TableCell>
                        <Badge
                          variant={
                            order.status === OrderStatus.COMPLETED
                              ? 'default'
                              : 'secondary'
                          }
                        >
                          {order.status}
                        </Badge>
                      </TableCell>
                      <TableCell>{formatDateTime(order.createdAt)}</TableCell>
                      <TableCell className="text-right">
                        {order.status === OrderStatus.COMPLETED && (
                          <Button
                            size="sm"
                            variant="destructive"
                            onClick={() => handleCancelClick(order)}
                          >
                            <XCircle className="mr-1 h-4 w-4" />
                            취소
                          </Button>
                        )}
                        {order.status === OrderStatus.CANCELLED && (
                          <span className="text-sm text-muted-foreground">
                            취소됨
                          </span>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>

              {/* 페이지네이션 */}
              <div className="flex items-center justify-between mt-4">
                <div className="text-sm text-muted-foreground">
                  전체 {data.totalElements}개 중 {page * 20 + 1}-
                  {Math.min((page + 1) * 20, data.totalElements)}개 표시
                </div>
                <div className="flex items-center gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setPage(page - 1)}
                    disabled={!hasPrevPage}
                  >
                    이전
                  </Button>
                  <span className="text-sm">
                    {page + 1} / {totalPages || 1}
                  </span>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setPage(page + 1)}
                    disabled={!hasNextPage}
                  >
                    다음
                  </Button>
                </div>
              </div>
            </>
          )}
        </CardContent>
      </Card>

      {/* 주문 취소 확인 Dialog */}
      <Dialog open={cancelDialogOpen} onOpenChange={setCancelDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>주문 취소</DialogTitle>
            <DialogDescription>
              이 주문을 취소하시겠습니까?
              <br />
              사용한 포인트가 환불되고 상품 재고가 복구됩니다.
            </DialogDescription>
          </DialogHeader>
          {orderToCancel && (
            <div className="space-y-2 py-4">
              <div className="flex justify-between">
                <span className="text-sm text-muted-foreground">주문번호</span>
                <span className="font-medium">{orderToCancel.id}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm text-muted-foreground">사용자</span>
                <span className="font-medium">{orderToCancel.userName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm text-muted-foreground">상품</span>
                <span className="font-medium">{orderToCancel.productName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm text-muted-foreground">환불 금액</span>
                <span className="font-medium text-green-600">
                  {orderToCancel.totalPrice.toLocaleString()}p
                </span>
              </div>
            </div>
          )}
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setCancelDialogOpen(false);
                setOrderToCancel(null);
              }}
            >
              닫기
            </Button>
            <Button
              variant="destructive"
              onClick={handleCancelConfirm}
              disabled={cancelMutation.isPending}
            >
              {cancelMutation.isPending ? '취소 중...' : '주문 취소'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
