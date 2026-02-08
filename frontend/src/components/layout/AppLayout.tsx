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

      {/* 메인 콘텐츠 영역 - 화면 중앙 정렬 */}
      <main className="flex items-center justify-center min-h-[calc(100vh-7.5rem)]">
        <div className="w-full max-w-screen-sm px-4 py-4">
          {children}
        </div>
      </main>

      <BottomNav />
    </div>
  );
}
