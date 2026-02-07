import { Link, useLocation } from 'react-router-dom';
import { cn } from '@/lib/utils';

const menuItems = [
  { path: '/', label: '대시보드' },
  { path: '/budget', label: '예산 관리' },
  { path: '/roulette', label: '룰렛 관리' },
  { path: '/products', label: '상품 관리' },
  { path: '/orders', label: '주문 관리' },
];

export default function Sidebar() {
  const location = useLocation();

  return (
    <aside className="w-64 min-h-screen bg-gray-900 text-white">
      <div className="p-6">
        <h1 className="text-2xl font-bold">Roulette Admin</h1>
      </div>

      <nav className="space-y-1 px-3">
        {menuItems.map((item) => (
          <Link
            key={item.path}
            to={item.path}
            className={cn(
              'block px-3 py-2 rounded-md text-sm font-medium transition-colors',
              location.pathname === item.path
                ? 'bg-gray-800 text-white'
                : 'text-gray-300 hover:bg-gray-800 hover:text-white'
            )}
          >
            {item.label}
          </Link>
        ))}
      </nav>
    </aside>
  );
}
