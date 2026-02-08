import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';
import { ko } from 'date-fns/locale';
import { useAuth } from '@/contexts/AuthContext';
import {
  getBalance,
  getPointHistory,
  withNavigation,
  getPointTypeLabel,
  getPointTypeColor,
  getDaysUntilExpiry,
  isExpiredPoint,
  isExpiringSoonPoint,
  isIncreaseType,
  type PointItem,
} from '@/api/points';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Clock } from 'lucide-react';
import AppLayout from '@/components/layout/AppLayout';
import { FullScreenLoading } from '@/components/LoadingSpinner';

export default function PointsPage() {
  const { user } = useAuth();
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 20;

  // 포인트 잔액 조회
  const { data: balanceData, isLoading: balanceLoading } = useQuery({
    queryKey: ['points-balance'],
    queryFn: getBalance,
  });

  // 포인트 내역 조회
  const { data: historyData, isLoading: historyLoading } = useQuery({
    queryKey: ['points-history', currentPage],
    queryFn: () => getPointHistory({ page: currentPage, size: pageSize }),
  });

  const balance = balanceData?.data;
  const historyResponse = historyData?.data;
  const history = historyResponse ? withNavigation(historyResponse) : null;

  // 로딩 상태
  if (balanceLoading || historyLoading) {
    return <FullScreenLoading message="포인트 정보를 불러오는 중..." />;
  }

  const hasExpiringPoints = balance && balance.expiringPoints.length > 0;

  return (
    <AppLayout title="💰 내 포인트">
      <div className="space-y-6">
        {/* 환영 메시지 */}
        <div className="text-center space-y-1">
          <p className="text-lg font-semibold text-gray-800">
            <span className="bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
              {user?.nickname}
            </span>
            님의 포인트 💎
          </p>
          <p className="text-sm text-gray-600">획득한 포인트와 사용 내역을 확인하세요</p>
        </div>

        {/* 포인트 잔액 카드 */}
        <Card className="backdrop-blur-lg bg-white/70 border-white/20 shadow-xl">
          <CardHeader>
            <CardTitle className="text-center text-lg">보유 포인트</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="text-center">
              <div className="text-5xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
                {balance?.totalBalance.toLocaleString() || 0}p
              </div>
            </div>
          </CardContent>
        </Card>

        {/* 만료 예정 알림 배너 */}
        {hasExpiringPoints && (
          <div className="bg-gradient-to-r from-orange-100 to-red-100 rounded-lg p-3 border border-orange-200">
            <div className="flex items-start gap-2">
              <span className="text-xl">⚠️</span>
              <div className="flex-1 space-y-2">
                <p className="text-sm font-semibold text-gray-800">
                  7일 내 만료 예정 포인트
                </p>
                {balance.expiringPoints.map((point) => {
                  const daysLeft = getDaysUntilExpiry(point.expiresAt);
                  return (
                    <div
                      key={point.id}
                      className="flex items-center justify-between text-xs text-gray-700"
                    >
                      <span>
                        {format(new Date(point.expiresAt), 'M월 d일', { locale: ko })} 만료
                      </span>
                      <div className="flex items-center gap-2">
                        <span className="font-semibold">{point.balance.toLocaleString()}p</span>
                        <span className="px-2 py-0.5 rounded-full bg-orange-200 text-orange-800 font-medium">
                          D-{daysLeft}
                        </span>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        )}

        {/* 포인트 내역 */}
        <Card className="backdrop-blur-lg bg-white/70 border-white/20 shadow-xl">
          <CardHeader>
            <CardTitle className="text-center text-lg">포인트 내역</CardTitle>
            <p className="text-center text-sm text-orange-600 font-medium mt-2">
              7일 내 만료 예정: {balance?.expiringPoints.reduce((sum, p) => sum + p.balance, 0).toLocaleString() || '0'}p
            </p>
          </CardHeader>
          <CardContent>
            {history && history.items.length > 0 ? (
              <>
                <div className="space-y-2">
                  {history.items.map((item) => (
                    <PointItemCard key={item.id} item={item} />
                  ))}
                </div>

                {/* 페이지네이션 */}
                {history.totalPages > 1 && (
                  <div className="mt-4 pt-4 border-t flex items-center justify-between">
                    <p className="text-xs text-gray-500">
                      {currentPage + 1} / {history.totalPages} 페이지
                      <span className="ml-1 text-gray-400">
                        (전체 {history.totalCount}건)
                      </span>
                    </p>
                    <div className="flex gap-2">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setCurrentPage((p) => p - 1)}
                        disabled={!history.hasPrevious}
                        className="h-8 px-3 text-xs"
                      >
                        이전
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setCurrentPage((p) => p + 1)}
                        disabled={!history.hasNext}
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
                <p className="text-4xl">📭</p>
                <p className="text-sm text-gray-600 font-medium">포인트 내역이 없습니다</p>
                <p className="text-xs text-gray-500">룰렛을 돌려서 포인트를 획득하세요!</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  );
}

// ============================================
// 🎨 PointItemCard 컴포넌트
// ============================================

interface PointItemCardProps {
  item: PointItem;
}

function PointItemCard({ item }: PointItemCardProps) {
  const isExpired = isExpiredPoint(item);
  const isExpiringSoon = isExpiringSoonPoint(item);
  const typeColor = getPointTypeColor(item.type);
  const typeLabel = getPointTypeLabel(item.type);
  const daysLeft = getDaysUntilExpiry(item.expiresAt);
  const isIncrease = isIncreaseType(item.type);  // EARN, REFUND

  return (
    <div
      className={`
        p-3 rounded-lg border transition-all
        ${
          isExpired
            ? 'bg-gray-50/50 border-gray-200 opacity-50'
            : 'bg-gradient-to-r from-purple-50/30 to-pink-50/30 border-purple-100/50'
        }
      `}
    >
      <div className="flex items-center justify-between">
        {/* 왼쪽: 타입 + 금액 */}
        <div className="flex items-center gap-2 flex-1">
          <div className="flex-1">
            <div className="flex items-center gap-2">
              <span className={`text-sm font-medium ${isExpired ? 'text-gray-400' : 'text-gray-700'}`}>
                {typeLabel}
              </span>
              <span className={`text-base font-bold ${isExpired ? 'text-gray-400' : typeColor}`}>
                {isIncrease ? '+' : '-'}
                {Math.abs(item.amount).toLocaleString()}p
              </span>
            </div>
            <div className="flex items-center gap-2 mt-0.5">
              <p className="text-xs text-gray-500">
                {format(new Date(item.issuedAt), 'M월 d일 HH:mm', { locale: ko })}
              </p>
              <span className="text-gray-300">•</span>
              <div className="flex items-center gap-1">
                <Clock className="w-3 h-3 text-gray-400" />
                <span className={`text-xs ${isExpired ? 'text-gray-400 line-through' : 'text-gray-500'}`}>
                  {format(new Date(item.expiresAt), 'M/d', { locale: ko })} 만료 예정
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* 오른쪽: 상태 배지 */}
        <div className="flex items-center gap-2">
          {isExpired ? (
            <Badge variant="secondary" className="text-xs px-2 py-0.5">
              만료
            </Badge>
          ) : isExpiringSoon ? (
            <Badge
              variant="outline"
              className="text-xs px-2 py-0.5 border-orange-300 text-orange-700 bg-orange-50"
            >
              D-{daysLeft}
            </Badge>
          ) : (
            <Badge variant="outline" className="text-xs px-2 py-0.5 border-green-300 text-green-700 bg-green-50">
              유효
            </Badge>
          )}
        </div>
      </div>
    </div>
  );
}
