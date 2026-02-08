import AppLayout from '@/components/layout/AppLayout';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';

export default function OrdersPage() {
  return (
    <AppLayout title="📦 주문 내역">
      <div className="space-y-4">
        <Card>
          <CardHeader>
            <CardTitle>주문 내역</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-gray-500">
              주문 내역 페이지는 곧 구현됩니다.
            </p>
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  );
}
