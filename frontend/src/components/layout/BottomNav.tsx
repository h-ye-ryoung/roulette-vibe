import { useLocation, useNavigate } from 'react-router-dom';
import { Dices, Wallet, ShoppingBag, Package } from 'lucide-react';
import { cn } from '@/lib/utils';

const navItems = [
  { path: '/', label: '홈', icon: Dices },
  { path: '/points', label: '포인트', icon: Wallet },
  { path: '/products', label: '상품', icon: ShoppingBag },
  { path: '/orders', label: '주문', icon: Package },
];

export default function BottomNav() {
  const location = useLocation();
  const navigate = useNavigate();

  return (
    <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 shadow-lg z-50">
      <div className="max-w-screen-sm mx-auto px-4">
        <div className="flex justify-around items-center h-16">
          {navItems.map((item) => {
            const isActive = location.pathname === item.path;
            const Icon = item.icon;

            return (
              <button
                key={item.path}
                onClick={() => navigate(item.path)}
                className={cn(
                  'flex flex-col items-center justify-center gap-1 px-4 py-2 rounded-lg transition-all',
                  isActive
                    ? 'text-purple-600'
                    : 'text-gray-500 hover:text-purple-500'
                )}
              >
                <Icon
                  className={cn(
                    'w-6 h-6 transition-transform',
                    isActive && 'scale-110'
                  )}
                />
                <span
                  className={cn(
                    'text-xs font-medium',
                    isActive && 'font-semibold'
                  )}
                >
                  {item.label}
                </span>
                {isActive && (
                  <div className="absolute -bottom-0.5 w-12 h-1 bg-gradient-to-r from-purple-600 to-pink-600 rounded-full" />
                )}
              </button>
            );
          })}
        </div>
      </div>
    </nav>
  );
}
