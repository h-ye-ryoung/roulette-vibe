import { useQuery } from '@tanstack/react-query';
import { getDashboard } from '@/api/dashboard';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { Skeleton } from '@/components/ui/skeleton';
import { Button } from '@/components/ui/button';

export default function DashboardPage() {
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['dashboard'],
    queryFn: getDashboard,
  });

  if (isLoading) {
    return <DashboardSkeleton />;
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] space-y-4">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-2">데이터를 불러올 수 없습니다</h2>
          <p className="text-gray-600 mb-4">
            {error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다'}
          </p>
          <Button onClick={() => refetch()}>다시 시도</Button>
        </div>
      </div>
    );
  }

  const dashboard = data?.data;
  if (!dashboard) {
    return null;
  }

  const usagePercentage = (dashboard.usedAmount / dashboard.dailyLimit) * 100;

  return (
    <div className="space-y-8">
      {/* 헤더 */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight">대시보드</h1>
        <p className="text-muted-foreground mt-2">
          {dashboard.budgetDate} 기준 룰렛 운영 현황
        </p>
      </div>

      {/* 통계 카드 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatCard
          title="일일 예산"
          value={dashboard.dailyLimit}
          suffix="p"
        />
        <StatCard
          title="남은 예산"
          value={dashboard.remaining}
          suffix="p"
          trend={usagePercentage > 80 ? 'warning' : 'normal'}
        />
        <StatCard
          title="사용한 예산"
          value={dashboard.usedAmount}
          suffix="p"
        />
        <StatCard
          title="참여자 수"
          value={dashboard.participantCount}
          suffix="명"
        />
      </div>

      {/* 예산 소진율 */}
      <Card>
        <CardHeader>
          <CardTitle>예산 소진율</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between text-sm">
            <span className="text-muted-foreground">
              {dashboard.usedAmount.toLocaleString()}p / {dashboard.dailyLimit.toLocaleString()}p
            </span>
            <span className="font-medium">
              {usagePercentage.toFixed(1)}%
            </span>
          </div>
          <Progress value={usagePercentage} className="h-3" />
          {usagePercentage >= 100 && (
            <p className="text-sm text-destructive font-medium">
              ⚠️ 예산이 모두 소진되었습니다
            </p>
          )}
          {usagePercentage >= 80 && usagePercentage < 100 && (
            <p className="text-sm text-orange-600 font-medium">
              ⚠️ 예산의 80% 이상이 사용되었습니다
            </p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

// 통계 카드 컴포넌트
function StatCard({
  title,
  value,
  suffix,
  trend = 'normal',
}: {
  title: string;
  value: number;
  suffix: string;
  trend?: 'normal' | 'warning';
}) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">
          {title}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div
          className={`text-2xl font-bold ${
            trend === 'warning' ? 'text-orange-600' : 'text-gray-900'
          }`}
        >
          {value.toLocaleString()}{suffix}
        </div>
      </CardContent>
    </Card>
  );
}

// 로딩 스켈레톤
function DashboardSkeleton() {
  return (
    <div className="space-y-8">
      <div>
        <Skeleton className="h-9 w-32" />
        <Skeleton className="h-5 w-64 mt-2" />
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {[1, 2, 3, 4].map((i) => (
          <Card key={i}>
            <CardHeader className="space-y-0 pb-2">
              <Skeleton className="h-4 w-24" />
            </CardHeader>
            <CardContent>
              <Skeleton className="h-8 w-32" />
            </CardContent>
          </Card>
        ))}
      </div>

      <Card>
        <CardHeader>
          <Skeleton className="h-6 w-32" />
        </CardHeader>
        <CardContent className="space-y-4">
          <Skeleton className="h-4 w-full" />
          <Skeleton className="h-3 w-full" />
        </CardContent>
      </Card>
    </div>
  );
}
