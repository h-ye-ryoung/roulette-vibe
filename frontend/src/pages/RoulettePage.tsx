import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '@/contexts/AuthContext';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog';
import AppLayout from '@/components/layout/AppLayout';
import BudgetCard from '@/components/BudgetCard';
import RouletteWheel from '@/components/RouletteWheel';
import { FullScreenLoading, ButtonLoading } from '@/components/LoadingSpinner';
import { getBudget, getStatus, spin, type SpinResponse } from '@/api/roulette';
import { AxiosError } from 'axios';

interface ApiError {
  code: string;
  message: string;
}

export default function RoulettePage() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [isSpinning, setIsSpinning] = useState(false);
  const [spinResult, setSpinResult] = useState<SpinResponse | null>(null);
  const [showResultModal, setShowResultModal] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // 예산 조회
  const { data: budget, isLoading: isBudgetLoading } = useQuery({
    queryKey: ['budget'],
    queryFn: getBudget,
  });

  // 룰렛 상태 조회 (참여 여부, 잔여 예산, 이력)
  const { data: status, isLoading: isStatusLoading } = useQuery({
    queryKey: ['roulette-status'],
    queryFn: getStatus,
  });

  // 룰렛 참여
  const spinMutation = useMutation({
    mutationFn: spin,
    onSuccess: (data) => {
      setSpinResult(data);
      setIsSpinning(true);
    },
    onError: (error: AxiosError<{ success: boolean; error?: ApiError }>) => {
      const errorCode = error.response?.data?.error?.code;
      const errorMsg = error.response?.data?.error?.message;

      if (errorCode === 'ALREADY_PARTICIPATED') {
        setErrorMessage('오늘은 이미 참여하셨습니다. 내일 다시 도전해주세요!');
      } else if (errorCode === 'BUDGET_EXHAUSTED') {
        setErrorMessage('오늘 예산이 모두 소진되었습니다.');
      } else {
        setErrorMessage(errorMsg || '룰렛 참여에 실패했습니다.');
      }
      setShowResultModal(true);
    },
  });

  const handleSpin = () => {
    if (status?.participated || isSpinning) return;
    setErrorMessage(null);
    spinMutation.mutate();
  };

  const handleSpinComplete = () => {
    setTimeout(() => {
      setIsSpinning(false);
      setShowResultModal(true);
      // 예산 및 상태 정보 갱신
      queryClient.invalidateQueries({ queryKey: ['budget'] });
      queryClient.invalidateQueries({ queryKey: ['roulette-status'] });
    }, 500);
  };

  const handleCloseModal = () => {
    setShowResultModal(false);
  };

  if (isBudgetLoading || isStatusLoading) {
    return <FullScreenLoading message="정보를 불러오는 중..." />;
  }

  return (
    <AppLayout title="🎰 포인트 룰렛">
      <div className="space-y-6">
        {/* 환영 메시지 */}
        <div className="text-center space-y-1">
          <p className="text-lg font-semibold text-gray-800">
            <span className="bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
              {user?.nickname}
            </span>
            님, 환영합니다! 👋
          </p>
          <p className="text-sm text-gray-600">행운의 룰렛을 돌려 포인트를 획득하세요!</p>
        </div>

        {/* 예산 카드 */}
        {budget && <BudgetCard budget={budget} />}

        {/* 오늘 참여 이력 */}
        {status?.history && (
          <div className="bg-gradient-to-r from-purple-100 to-pink-100 rounded-lg p-3 border border-purple-200">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span className="text-lg">✅</span>
                <p className="text-sm font-medium text-gray-700">오늘 참여 완료</p>
              </div>
              <p className="text-lg font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
                +{status.history.amount.toLocaleString()}p
              </p>
            </div>
          </div>
        )}

        {/* 회수 차감 알림 */}
        {spinResult && spinResult.recoveredAmount > 0 && showResultModal && !errorMessage && (
          <div className="bg-gradient-to-r from-blue-100 to-purple-100 rounded-lg p-4 border border-blue-200">
            <p className="text-sm text-gray-700">
              💡 회수 예정 포인트 <strong>{spinResult.recoveredAmount.toLocaleString()}p</strong>가 차감되었습니다.
            </p>
            <p className="text-xs text-gray-500 mt-1">
              당첨 금액: {spinResult.amount.toLocaleString()}p → 실제 지급: {spinResult.actualGrantedAmount.toLocaleString()}p
            </p>
          </div>
        )}

        {/* 룰렛 휠 */}
        <div className="py-4">
          <RouletteWheel
            isSpinning={isSpinning}
            targetAmount={spinResult?.amount}
            onSpinComplete={handleSpinComplete}
          />
        </div>

        {/* 참여 버튼 */}
        <div className="flex flex-col items-center space-y-3">
          <Button
            onClick={handleSpin}
            disabled={status?.participated || isSpinning || spinMutation.isPending}
            className="w-full max-w-xs h-14 text-lg font-semibold bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 disabled:opacity-50 disabled:cursor-not-allowed shadow-lg transform transition-transform hover:scale-105"
          >
            {isSpinning || spinMutation.isPending ? (
              <ButtonLoading />
            ) : status?.participated ? (
              '오늘 참여 완료'
            ) : (
              '룰렛 돌리기 →'
            )}
          </Button>
          <div className="text-center space-y-1">
            <p className="text-sm text-gray-500">
              {status?.participated ? '내일 다시 도전해주세요!' : '하루 1번 참여 가능'}
            </p>
            {status && (
              <p className="text-xs text-gray-400">
                잔여 예산: {status.remainingBudget.toLocaleString()}p
              </p>
            )}
          </div>
        </div>
      </div>

      {/* 결과 모달 */}
      <Dialog open={showResultModal} onOpenChange={setShowResultModal}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="text-center text-2xl">
              {errorMessage
                ? '⚠️ 알림'
                : spinResult?.amount && spinResult.amount >= 1000
                ? '🎊 대박!'
                : '🎉 당첨!'}
            </DialogTitle>
            <DialogDescription className="text-center text-lg pt-4">
              {errorMessage ? (
                <span className="text-gray-700">{errorMessage}</span>
              ) : (
                <div className="space-y-3">
                  <div className="text-4xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
                    {spinResult?.amount.toLocaleString()}p
                  </div>
                  <div className="text-gray-700">
                    포인트를 획득했습니다!
                  </div>
                  {spinResult?.recoveredAmount !== undefined && spinResult.recoveredAmount > 0 && (
                    <div className="bg-blue-50 rounded-lg p-3 text-sm space-y-1">
                      <p className="text-blue-700 font-medium">
                        💡 회수 예정 {spinResult.recoveredAmount.toLocaleString()}p 차감
                      </p>
                      <p className="text-xs text-gray-600">
                        실제 지급: {spinResult.actualGrantedAmount.toLocaleString()}p
                      </p>
                    </div>
                  )}
                  {spinResult?.remainingBudget !== undefined && (
                    <div className="text-sm text-gray-500 pt-2">
                      남은 예산: {spinResult.remainingBudget.toLocaleString()}p
                    </div>
                  )}
                </div>
              )}
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              onClick={handleCloseModal}
              className="w-full bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700"
            >
              확인
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </AppLayout>
  );
}
