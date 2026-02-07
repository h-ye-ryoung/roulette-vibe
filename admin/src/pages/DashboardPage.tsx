import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

export default function DashboardPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">대시보드</h1>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">일일 예산</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">100,000p</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">남은 예산</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">75,000p</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">사용한 예산</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">25,000p</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">참여자 수</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">42명</div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
