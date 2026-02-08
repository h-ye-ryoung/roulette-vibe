import { useAuth } from '@/contexts/AuthContext';
import { Button } from '@/components/ui/button';
import { LogOut } from 'lucide-react';

interface HeaderProps {
  title: string;
  showLogout?: boolean;
}

export default function Header({ title, showLogout = true }: HeaderProps) {
  const { logout } = useAuth();

  const handleLogout = async () => {
    await logout();
    window.location.href = '/login';
  };

  return (
    <header className="sticky top-0 bg-white/80 backdrop-blur-lg border-b border-gray-200 z-40 shadow-sm">
      <div className="max-w-screen-sm mx-auto px-4 h-14 flex items-center justify-between">
        <h1 className="text-lg font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
          {title}
        </h1>
        {showLogout && (
          <Button
            onClick={handleLogout}
            variant="ghost"
            size="sm"
            className="text-gray-600 hover:text-purple-600"
          >
            <LogOut className="w-4 h-4 mr-2" />
            로그아웃
          </Button>
        )}
      </div>
    </header>
  );
}
