import AppLayout from '@/components/layout/AppLayout';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';

export default function PointsPage() {
  return (
    <AppLayout title="💰 내 포인트">
      <div className="space-y-4">
        <Card>
          <CardHeader>
            <CardTitle>총 포인트</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-4xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
              0p
            </div>
            <p className="text-sm text-gray-500 mt-2">
              포인트 내역 페이지는 곧 구현됩니다.
            </p>
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  );
}
