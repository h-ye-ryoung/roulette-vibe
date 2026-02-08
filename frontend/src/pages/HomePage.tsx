import { useAuth } from '@/contexts/AuthContext';
import { Button } from '@/components/ui/button';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';

export default function HomePage() {
  const { user, logout } = useAuth();

  const handleLogout = async () => {
    await logout();
    window.location.href = '/login';
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 to-pink-100 p-4">
      <div className="max-w-4xl mx-auto pt-8">
        <Card>
          <CardHeader>
            <CardTitle className="text-2xl">
              환영합니다, {user?.nickname}님! 🎉
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <p className="text-muted-foreground">
              룰렛 페이지는 곧 구현됩니다.
            </p>
            <Button onClick={handleLogout} variant="outline">
              로그아웃
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
