import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { getBudget, updateBudget } from '@/api/budget';
import { getRouletteHistory, cancelRoulette } from '@/api/roulette';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Badge } from '@/components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';

const budgetSchema = z.object({
  dailyLimit: z.number()
    .min(1000, '예산은 1,000p 이상이어야 합니다')
    .max(10000000, '예산은 10,000,000p 이하여야 합니다'),
});

type BudgetFormData = z.infer<typeof budgetSchema>;

export default function BudgetPage() {
  const queryClient = useQueryClient();
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [dateFilter, setDateFilter] = useState('');
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [selectedHistoryId, setSelectedHistoryId] = useState<number | null>(null);

  const { data: budgetData, isLoading: budgetLoading, error: budgetError } = useQuery({
    queryKey: ['budget'],
    queryFn: getBudget,
  });

  const { data: historyData, isLoading: historyLoading } = useQuery({
    queryKey: ['roulette-history', currentPage, dateFilter],
    queryFn: () => getRouletteHistory({ page: currentPage, size: 20, date: dateFilter || undefined }),
  });

  const { register, handleSubmit, formState: { errors } } = useForm<BudgetFormData>({
    resolver: zodResolver(budgetSchema),
  });

  const budgetMutation = useMutation({
    mutationFn: updateBudget,
    onSuccess: () => {
      setSuccessMessage('예산이 변경되었습니다');
      setErrorMessage('');
      queryClient.invalidateQueries({ queryKey: ['budget'] });
      setTimeout(() => setSuccessMessage(''), 3000);
    },
    onError: (error: Error) => {
      setErrorMessage(error.message || '예산 변경에 실패했습니다');
      setSuccessMessage('');
      setTimeout(() => setErrorMessage(''), 3000);
    },
  });

  const cancelMutation = useMutation({
    mutationFn: cancelRoulette,
    onSuccess: () => {
      setSuccessMessage('룰렛 참여가 취소되었습니다');
      setErrorMessage('');
      queryClient.invalidateQueries({ queryKey: ['roulette-history'] });
      queryClient.invalidateQueries({ queryKey: ['budget'] });
      setCancelDialogOpen(false);
      setTimeout(() => setSuccessMessage(''), 3000);
    },
    onError: (error: Error) => {
      setErrorMessage(error.message || '룰렛 취소에 실패했습니다');
      setSuccessMessage('');
      setTimeout(() => setErrorMessage(''), 3000);
    },
  });

  const onBudgetSubmit = (formData: BudgetFormData) => {
    budgetMutation.mutate(formData);
  };

  const handleCancelClick = (historyId: number) => {
    setSelectedHistoryId(historyId);
    setCancelDialogOpen(true);
  };

  const handleCancelConfirm = () => {
    if (selectedHistoryId) {
      cancelMutation.mutate(selectedHistoryId);
    }
  };

  const handleDateFilterReset = () => {
    setDateFilter('');
    setCurrentPage(0);
  };

  if (budgetLoading) {
    return <BudgetSkeleton />;
  }

  if (budgetError) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] space-y-4">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-2">데이터를 불러올 수 없습니다</h2>
          <p className="text-gray-600 mb-4">
            {budgetError instanceof Error ? budgetError.message : '알 수 없는 오류가 발생했습니다'}
          </p>
        </div>
      </div>
    );
  }

  const budget = budgetData?.data;
  const history = historyData?.data;

  if (!budget) {
    return null;
  }

  return (
    <div className="space-y-8">
      {/* 헤더 */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight">예산 관리</h1>
        <p className="text-muted-foreground mt-2">
          일일 룰렛 예산과 참여 내역을 관리합니다
        </p>
      </div>

      {/* 알림 메시지 */}
      {successMessage && (
        <div className="bg-green-50 border border-green-200 text-green-800 px-4 py-3 rounded-md">
          {successMessage}
        </div>
      )}
      {errorMessage && (
        <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-md">
          {errorMessage}
        </div>
      )}

      <div className="grid gap-6 md:grid-cols-2">
        {/* 현재 예산 표시 */}
        <Card>
          <CardHeader>
            <CardTitle>현재 예산</CardTitle>
            <CardDescription>{budget.budgetDate} 기준</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <p className="text-sm text-muted-foreground">일일 예산 한도</p>
              <p className="text-2xl font-bold">{budget.dailyLimit.toLocaleString()}p</p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">남은 예산</p>
              <p className="text-2xl font-bold text-blue-600">
                {budget.remaining.toLocaleString()}p
              </p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">사용한 예산</p>
              <p className="text-2xl font-bold text-gray-600">
                {(budget.dailyLimit - budget.remaining).toLocaleString()}p
              </p>
            </div>
          </CardContent>
        </Card>

        {/* 예산 변경 폼 */}
        <Card>
          <CardHeader>
            <CardTitle>예산 변경</CardTitle>
            <CardDescription>
              ⚠️ 예산 변경은 다음 날(KST)부터 적용됩니다
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit(onBudgetSubmit)} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="dailyLimit">새 일일 예산 (포인트)</Label>
                <Input
                  id="dailyLimit"
                  type="number"
                  placeholder="예: 100000"
                  defaultValue={budget.dailyLimit}
                  {...register('dailyLimit', { valueAsNumber: true })}
                  className={errors.dailyLimit ? 'border-red-500' : ''}
                />
                {errors.dailyLimit && (
                  <p className="text-sm text-red-600">{errors.dailyLimit.message}</p>
                )}
                <p className="text-xs text-muted-foreground">
                  1,000p ~ 10,000,000p 범위로 설정 가능
                </p>
              </div>

              <Button
                type="submit"
                className="w-full"
                disabled={budgetMutation.isPending}
              >
                {budgetMutation.isPending ? '저장 중...' : '예산 변경'}
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>

      {/* 룰렛 참여 내역 */}
      <Card>
        <CardHeader>
          <CardTitle>룰렛 참여 내역</CardTitle>
          <CardDescription>참여 내역을 조회하고 취소할 수 있습니다</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* 필터 영역 */}
          <div className="flex gap-2">
            <Input
              type="date"
              value={dateFilter}
              onChange={(e) => {
                setDateFilter(e.target.value);
                setCurrentPage(0);
              }}
              className="max-w-xs"
              placeholder="날짜 필터"
            />
            <Button variant="outline" onClick={handleDateFilterReset}>
              필터 초기화
            </Button>
          </div>

          {/* 테이블 */}
          {historyLoading ? (
            <div className="space-y-2">
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
            </div>
          ) : history?.items && history.items.length > 0 ? (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>ID</TableHead>
                    <TableHead>사용자명</TableHead>
                    <TableHead>지급 포인트</TableHead>
                    <TableHead>남은 포인트</TableHead>
                    <TableHead>날짜</TableHead>
                    <TableHead>상태</TableHead>
                    <TableHead>액션</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {history.items.map((item) => (
                    <TableRow key={item.historyId}>
                      <TableCell>{item.historyId}</TableCell>
                      <TableCell>{item.userName}</TableCell>
                      <TableCell>{item.originalAmount.toLocaleString()}p</TableCell>
                      <TableCell>{item.reclaimedAmount.toLocaleString()}p</TableCell>
                      <TableCell>{item.spinDate}</TableCell>
                      <TableCell>
                        <Badge variant={item.status === 'ACTIVE' ? 'success' : 'secondary'}>
                          {item.status === 'ACTIVE' ? '활성' : '취소됨'}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        {item.status === 'ACTIVE' && (
                          <Button
                            variant="destructive"
                            size="sm"
                            onClick={() => handleCancelClick(item.historyId)}
                          >
                            취소
                          </Button>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>

              {/* 페이지네이션 */}
              <div className="flex items-center justify-between">
                <p className="text-sm text-muted-foreground">
                  {history.pageInfo.currentPage + 1} / {history.pageInfo.totalPages} 페이지
                  {' '}(전체 {history.pageInfo.totalElements}건)
                </p>
                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
                    disabled={!history.pageInfo.hasPrevious}
                  >
                    이전
                  </Button>
                  <Button
                    variant="outline"
                    onClick={() => setCurrentPage((p) => p + 1)}
                    disabled={!history.pageInfo.hasNext}
                  >
                    다음
                  </Button>
                </div>
              </div>
            </>
          ) : (
            <p className="text-center text-muted-foreground py-8">
              룰렛 참여 내역이 없습니다
            </p>
          )}
        </CardContent>
      </Card>

      {/* 취소 확인 다이얼로그 */}
      <Dialog open={cancelDialogOpen} onOpenChange={setCancelDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>룰렛 참여 취소</DialogTitle>
            <DialogDescription>
              이 룰렛 참여를 취소하시겠습니까? 남은 포인트만 회수됩니다.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setCancelDialogOpen(false)}>
              취소
            </Button>
            <Button
              variant="destructive"
              onClick={handleCancelConfirm}
              disabled={cancelMutation.isPending}
            >
              {cancelMutation.isPending ? '처리 중...' : '확인'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

// 로딩 스켈레톤
function BudgetSkeleton() {
  return (
    <div className="space-y-8">
      <div>
        <Skeleton className="h-9 w-32" />
        <Skeleton className="h-5 w-64 mt-2" />
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        {[1, 2].map((i) => (
          <Card key={i}>
            <CardHeader>
              <Skeleton className="h-6 w-32" />
              <Skeleton className="h-4 w-48 mt-2" />
            </CardHeader>
            <CardContent className="space-y-4">
              <Skeleton className="h-16 w-full" />
              <Skeleton className="h-16 w-full" />
              <Skeleton className="h-16 w-full" />
            </CardContent>
          </Card>
        ))}
      </div>

      <Card>
        <CardHeader>
          <Skeleton className="h-6 w-48" />
          <Skeleton className="h-4 w-64 mt-2" />
        </CardHeader>
        <CardContent>
          <Skeleton className="h-64 w-full" />
        </CardContent>
      </Card>
    </div>
  );
}
