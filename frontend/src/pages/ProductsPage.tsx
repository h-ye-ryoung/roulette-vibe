import AppLayout from '@/components/layout/AppLayout';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';

export default function ProductsPage() {
  return (
    <AppLayout title="🛍️ 상품 목록">
      <div className="space-y-4">
        <Card>
          <CardHeader>
            <CardTitle>상품 목록</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-gray-500">
              상품 목록 페이지는 곧 구현됩니다.
            </p>
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  );
}
