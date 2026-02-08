import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '@/components/ui/card';

export default function LoginPage() {
  const [nickname, setNickname] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!nickname.trim()) {
      setError('닉네임을 입력해주세요.');
      return;
    }

    if (nickname.length > 50) {
      setError('닉네임은 50자 이하로 입력해주세요.');
      return;
    }

    setIsLoading(true);
    try {
      await login(nickname.trim());
      navigate('/');
    } catch (err: any) {
      console.error('로그인 에러:', err);
      console.error('에러 응답:', err.response?.data);
      const errorMessage = err.response?.data?.error?.message || err.message || '로그인에 실패했습니다.';
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="space-y-1">
          <CardTitle className="text-2xl font-bold text-center">
            🎰 포인트 룰렛
          </CardTitle>
          <CardDescription className="text-center">
            닉네임을 입력하고 룰렛을 돌려보세요!
          </CardDescription>
        </CardHeader>
        <form onSubmit={handleSubmit}>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="nickname">닉네임</Label>
              <Input
                id="nickname"
                type="text"
                placeholder="닉네임을 입력하세요"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                disabled={isLoading}
                autoFocus
              />
            </div>
            {error && (
              <div className="text-sm text-red-500">
                {error}
              </div>
            )}
          </CardContent>
          <CardFooter>
            <Button
              type="submit"
              className="w-full"
              disabled={isLoading}
            >
              {isLoading ? '로그인 중...' : '시작하기'}
            </Button>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
