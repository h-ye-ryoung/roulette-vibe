import { cn } from '@/lib/utils';

interface LoadingSpinnerProps {
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

export default function LoadingSpinner({ size = 'md', className }: LoadingSpinnerProps) {
  const sizeClasses = {
    sm: 'h-6 w-6 border-2',
    md: 'h-12 w-12 border-4',
    lg: 'h-16 w-16 border-4',
  };

  return (
    <div
      className={cn(
        'animate-spin rounded-full border-purple-200 border-t-purple-600',
        sizeClasses[size],
        className
      )}
    />
  );
}

// 전체 화면 로딩
export function FullScreenLoading({ message }: { message?: string }) {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center gap-4 bg-gradient-to-br from-purple-50 to-pink-50">
      <LoadingSpinner size="lg" />
      {message && (
        <p className="text-slate-600 text-lg animate-pulse">{message}</p>
      )}
    </div>
  );
}

// 버튼 내 로딩 (점 3개)
export function ButtonLoading() {
  return (
    <span className="flex gap-1 items-center justify-center">
      <span className="w-2 h-2 bg-current rounded-full animate-bounce" style={{ animationDelay: '0ms' }} />
      <span className="w-2 h-2 bg-current rounded-full animate-bounce" style={{ animationDelay: '150ms' }} />
      <span className="w-2 h-2 bg-current rounded-full animate-bounce" style={{ animationDelay: '300ms' }} />
    </span>
  );
}
