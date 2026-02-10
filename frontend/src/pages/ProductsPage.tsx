import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '@/contexts/AuthContext';
import {
  getProducts,
  createOrder,
  withPurchaseInfo,
  type ProductWithPurchaseInfo,
} from '@/api/products';
import { getBalance } from '@/api/points';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { useToast } from '@/hooks/use-toast';
import { ShoppingCart, Package } from 'lucide-react';
import AppLayout from '@/components/layout/AppLayout';
import { FullScreenLoading } from '@/components/LoadingSpinner';

export default function ProductsPage() {
  const { user } = useAuth();
  const { toast } = useToast();
  const queryClient = useQueryClient();

  const [selectedProduct, setSelectedProduct] = useState<ProductWithPurchaseInfo | null>(null);
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  // 포인트 잔액 조회
  const { data: balanceData, isLoading: balanceLoading } = useQuery({
    queryKey: ['points-balance'],
    queryFn: getBalance,
  });

  // 상품 목록 조회
  const { data: productsData, isLoading: productsLoading } = useQuery({
    queryKey: ['products'],
    queryFn: getProducts,
  });

  // 상품 주문 mutation
  const orderMutation = useMutation({
    mutationFn: (productId: number) => createOrder(productId),
    onSuccess: (response) => {
      // 포인트 잔액 및 상품 목록 갱신
      queryClient.invalidateQueries({ queryKey: ['points-balance'] });
      queryClient.invalidateQueries({ queryKey: ['products'] });
      queryClient.invalidateQueries({ queryKey: ['points-history'] });

      toast({
        title: '🎉 구매 완료!',
        description: `${response.data.productName}을(를) 구매했습니다. 남은 포인트: ${response.data.remainingBalance.toLocaleString()}p`,
        variant: 'default',
      });

      setIsDialogOpen(false);
      setSelectedProduct(null);
    },
    onError: (error: any) => {
      const errorCode = error.response?.data?.error?.code;
      let errorMessage = '구매에 실패했습니다.';

      if (errorCode === 'INSUFFICIENT_POINTS') {
        errorMessage = '포인트가 부족합니다.';
      } else if (errorCode === 'PRODUCT_OUT_OF_STOCK') {
        errorMessage = '상품 재고가 부족합니다.';
      } else if (errorCode === 'PRODUCT_NOT_FOUND') {
        errorMessage = '상품을 찾을 수 없습니다.';
      }

      toast({
        title: '구매 실패',
        description: errorMessage,
        variant: 'destructive',
      });

      setIsDialogOpen(false);
      setSelectedProduct(null);
    },
  });

  const balance = balanceData?.data;
  const products = productsData?.data?.products || [];
  const userBalance = balance?.totalBalance || 0;

  // 구매 정보가 포함된 상품 목록
  const productsWithInfo: ProductWithPurchaseInfo[] = products.map((product) =>
    withPurchaseInfo(product, userBalance)
  );

  // 로딩 상태
  if (balanceLoading || productsLoading) {
    return <FullScreenLoading message="상품 정보를 불러오는 중..." />;
  }

  // 구매 버튼 클릭 핸들러
  const handlePurchaseClick = (product: ProductWithPurchaseInfo) => {
    setSelectedProduct(product);
    setIsDialogOpen(true);
  };

  // 구매 확정 핸들러
  const handleConfirmPurchase = () => {
    if (selectedProduct) {
      orderMutation.mutate(selectedProduct.id);
    }
  };

  return (
    <AppLayout title="🛍️ 상품 구매">
      <div className="space-y-6 pb-32">
        {/* 환영 메시지 */}
        <div className="text-center space-y-1">
          <p className="text-lg font-semibold text-gray-800">
            <span className="bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
              {user?.nickname}
            </span>
            님의 쇼핑 💎
          </p>
          <p className="text-sm text-gray-600">포인트로 원하는 상품을 구매하세요</p>
        </div>

        {/* 내 포인트 카드 */}
        <Card className="backdrop-blur-lg bg-white/70 border-white/20 shadow-xl">
          <CardHeader>
            <CardTitle className="text-center text-lg">보유 포인트</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-center">
              <div className="text-4xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
                {userBalance.toLocaleString()}p
              </div>
            </div>
          </CardContent>
        </Card>

        {/* 상품 목록 */}
        <Card className="backdrop-blur-lg bg-white/70 border-white/20 shadow-xl">
          <CardHeader>
            <CardTitle className="text-center text-lg">상품 목록</CardTitle>
          </CardHeader>
          <CardContent>
            {productsWithInfo.length > 0 ? (
              <div className="space-y-3">
                {productsWithInfo.map((product) => (
                  <ProductCard
                    key={product.id}
                    product={product}
                    onPurchaseClick={handlePurchaseClick}
                    isPurchasing={orderMutation.isPending && selectedProduct?.id === product.id}
                  />
                ))}
              </div>
            ) : (
              <div className="py-8 text-center space-y-2">
                <p className="text-4xl">📦</p>
                <p className="text-sm text-gray-600 font-medium">구매 가능한 상품이 없습니다</p>
                <p className="text-xs text-gray-500">나중에 다시 확인해주세요!</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* 구매 확인 다이얼로그 */}
      <AlertDialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>상품 구매 확인</AlertDialogTitle>
            <AlertDialogDescription className="space-y-2">
              <p className="font-semibold text-gray-800">{selectedProduct?.name}</p>
              <p className="text-sm">가격: {selectedProduct?.price.toLocaleString()}p</p>
              <p className="text-sm">
                구매 후 잔액: {((userBalance || 0) - (selectedProduct?.price || 0)).toLocaleString()}p
              </p>
              <p className="text-xs text-gray-500 mt-2">구매하시겠습니까?</p>
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>취소</AlertDialogCancel>
            <AlertDialogAction onClick={handleConfirmPurchase} disabled={orderMutation.isPending}>
              {orderMutation.isPending ? '구매 중...' : '구매'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </AppLayout>
  );
}

// ============================================
// 🎨 ProductCard 컴포넌트
// ============================================

interface ProductCardProps {
  product: ProductWithPurchaseInfo;
  onPurchaseClick: (product: ProductWithPurchaseInfo) => void;
  isPurchasing: boolean;
}

function ProductCard({ product, onPurchaseClick, isPurchasing }: ProductCardProps) {
  return (
    <div className="p-3 rounded-lg border bg-gradient-to-r from-purple-50/30 to-pink-50/30 border-purple-100/50 transition-all hover:shadow-md">
      <div className="flex items-start gap-3">
        {/* 상품 아이콘 */}
        <div className="flex-shrink-0 w-10 h-10 rounded-lg bg-gradient-to-br from-purple-100 to-pink-100 flex items-center justify-center">
          <Package className="w-5 h-5 text-purple-600" />
        </div>

        {/* 상품 정보 */}
        <div className="flex-1 min-w-0 space-y-1.5">
          {/* 상품명 - truncate 제거, 자연스럽게 줄바꿈 */}
          <h3 className="text-sm font-semibold text-gray-800 leading-tight break-words">
            {product.name}
          </h3>

          {/* 가격 + 뱃지 */}
          <div className="space-y-1.5">
            <span className="text-base font-bold text-purple-600 whitespace-nowrap">
              {product.price.toLocaleString()}p
            </span>

            {/* 뱃지들 (별도 줄) */}
            {(product.stock <= 3 || product.insufficientPoints) && (
              <div className="flex items-center gap-2 flex-wrap">
                {product.stock <= 3 && (
                  <Badge variant="outline" className="text-xs px-1.5 py-0 border-orange-300 text-orange-700 bg-orange-50 whitespace-nowrap">
                    재고 {product.stock}개
                  </Badge>
                )}

                {product.insufficientPoints && (
                  <Badge variant="outline" className="text-xs px-1.5 py-0 border-red-300 text-red-700 bg-red-50 whitespace-nowrap">
                    포인트 부족
                  </Badge>
                )}
              </div>
            )}
          </div>

          {/* 설명 (옵션) */}
          {product.description && (
            <p className="text-xs text-gray-600 line-clamp-2 leading-relaxed">{product.description}</p>
          )}
        </div>

        {/* 구매 버튼 */}
        <div className="flex-shrink-0">
          <Button
            size="sm"
            onClick={() => onPurchaseClick(product)}
            disabled={!product.canPurchase || isPurchasing}
            className="bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 disabled:opacity-50 text-xs px-3 py-2 h-auto"
          >
            {isPurchasing ? (
              <span className="text-xs">구매중</span>
            ) : (
              <>
                <ShoppingCart className="w-3.5 h-3.5 mr-1" />
                구매
              </>
            )}
          </Button>
        </div>
      </div>
    </div>
  );
}
