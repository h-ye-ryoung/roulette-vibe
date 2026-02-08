import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import type { BudgetResponse } from '@/api/roulette';

interface BudgetCardProps {
  budget: BudgetResponse;
}

export default function BudgetCard({ budget }: BudgetCardProps) {
  const percentage = (budget.used / budget.limitAmount) * 100;

  return (
    <Card className="backdrop-blur-lg bg-white/70 border-white/20 shadow-xl">
      <CardHeader>
        <CardTitle className="text-center text-lg">오늘 남은 예산</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="text-center">
          <div className="text-5xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
            {budget.remaining.toLocaleString()}p
          </div>
        </div>
        <Progress value={percentage} className="h-3" />
        <p className="text-sm text-center text-muted-foreground">
          {budget.limitAmount.toLocaleString()}p 중{' '}
          {budget.used.toLocaleString()}p 사용
        </p>
      </CardContent>
    </Card>
  );
}
