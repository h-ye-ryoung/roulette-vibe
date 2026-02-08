import { type ReactNode } from 'react';
import Header from './Header';
import BottomNav from './BottomNav';

interface AppLayoutProps {
  children: ReactNode;
  title: string;
  showLogout?: boolean;
}

export default function AppLayout({ children, title, showLogout = true }: AppLayoutProps) {
  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-purple-50">
      <Header title={title} showLogout={showLogout} />

      {/* 메인 콘텐츠 영역 */}
      <main className="pb-20 pt-4">
        <div className="max-w-screen-sm mx-auto px-4">
          {children}
        </div>
      </main>

      <BottomNav />
    </div>
  );
}
