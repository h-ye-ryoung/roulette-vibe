import { useAuth } from '@/contexts/AuthContext';
import { Button } from '@/components/ui/button';

export default function Header() {
  const { user, logout } = useAuth();

  const handleLogout = async () => {
    await logout();
    window.location.href = '/login';
  };

  return (
    <header className="h-16 border-b bg-white flex items-center justify-between px-6">
      <div className="text-lg font-medium">
        환영합니다, {user?.nickname}님
      </div>

      <Button variant="outline" onClick={handleLogout}>
        로그아웃
      </Button>
    </header>
  );
}
