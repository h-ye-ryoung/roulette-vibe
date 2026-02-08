import React, { createContext, useContext, useState, useEffect } from 'react';
import { type LoginResponse, getCurrentUser, login as apiLogin, logout as apiLogout } from '@/api/auth';

interface AuthContextType {
  user: LoginResponse | null;
  isLoading: boolean;
  login: (nickname: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<LoginResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // 페이지 로드 시 현재 사용자 정보 조회
    getCurrentUser()
      .then((userData) => setUser(userData))
      .catch(() => setUser(null))
      .finally(() => setIsLoading(false));
  }, []);

  const login = async (nickname: string) => {
    const userData = await apiLogin(nickname);
    setUser(userData);
  };

  const logout = async () => {
    await apiLogout();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, isLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
