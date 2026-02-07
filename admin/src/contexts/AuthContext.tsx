import { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import { apiClient } from '@/api/client';
import type { User } from '@/types/models';

interface AuthContextType {
  user: User | null;
  login: (nickname: string) => Promise<void>;
  logout: () => Promise<void>;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // 페이지 로드 시 세션 확인
  useEffect(() => {
    checkSession();
  }, []);

  async function checkSession() {
    try {
      const response = await apiClient.get('/api/auth/me');
      if (response.data.success && response.data.data.role === 'ADMIN') {
        setUser(response.data.data);
      }
    } catch (error) {
      // 세션 없음 또는 만료
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }

  async function login(nickname: string) {
    const response = await apiClient.post('/api/auth/login', { nickname });
    if (response.data.data.role !== 'ADMIN') {
      throw new Error('어드민 권한이 없습니다');
    }
    setUser(response.data.data);
  }

  async function logout() {
    await apiClient.post('/api/auth/logout');
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, login, logout, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}
