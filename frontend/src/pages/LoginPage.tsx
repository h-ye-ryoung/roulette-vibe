import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { ButtonLoading } from '@/components/LoadingSpinner';

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
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-purple-50 via-pink-50 to-purple-50 p-4">
      {/* 로그인 카드 - 유리모피즘 */}
      <div className="w-full max-w-md">
        <div className="bg-white/80 backdrop-blur-lg rounded-2xl shadow-2xl border border-white/20 overflow-hidden">
          {/* 헤더 */}
          <div className="px-8 pt-8 pb-6 text-center space-y-3">
            <div className="text-5xl mb-2">🎰</div>
            <h1 className="text-3xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
              포인트 룰렛
            </h1>
            <p className="text-gray-600">
              닉네임을 입력하고 행운의 룰렛을 돌려보세요!
            </p>
          </div>

          {/* 폼 */}
          <form onSubmit={handleSubmit} className="px-8 pb-8 space-y-6">
            {/* 닉네임 입력 */}
            <div className="space-y-2">
              <label
                htmlFor="nickname"
                className="block text-sm font-medium text-gray-700"
              >
                닉네임
              </label>
              <Input
                id="nickname"
                type="text"
                placeholder="닉네임을 입력하세요"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                disabled={isLoading}
                autoFocus
                className="h-12 text-base border-gray-300 focus:border-purple-500 focus:ring-purple-500"
              />
            </div>

            {/* 에러 메시지 */}
            {error && (
              <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-red-600">
                {error}
              </div>
            )}

            {/* 시작 버튼 */}
            <Button
              type="submit"
              disabled={isLoading}
              className="w-full h-12 text-base font-semibold bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 shadow-lg hover:shadow-xl transform transition-all hover:scale-[1.02] disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100"
            >
              {isLoading ? <ButtonLoading /> : '시작하기 →'}
            </Button>
          </form>

          {/* 하단 장식 */}
          <div className="h-2 bg-gradient-to-r from-purple-600 via-pink-500 to-purple-600" />
        </div>

        {/* 안내 텍스트 */}
        <p className="mt-6 text-center text-sm text-gray-600">
          닉네임이 없으면 자동으로 새 계정이 생성됩니다
        </p>
      </div>
    </div>
  );
}
