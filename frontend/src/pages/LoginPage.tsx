import { FormEvent, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { authApi } from '@/api/auth';
import { usersApi } from '@/api/users';
import { useAuthStore } from '@/store/auth';
import { extractErrorMessage } from '@/api/client';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const { setTokens, setProfile } = useAuthStore();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: string } | null)?.from ?? '/';

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const auth = await authApi.login({ email, password });
      setTokens(auth.accessToken, auth.refreshToken);
      // Fetch profile to know role (USER/ADMIN)
      try {
        const me = await usersApi.me();
        setProfile(me.email, me.name, me.role);
      } catch {
        setProfile(auth.email, auth.name);
      }
      navigate(from, { replace: true });
    } catch (err) {
      setError(extractErrorMessage(err, '로그인에 실패했습니다.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto max-w-sm py-10">
      <h1 className="text-2xl font-bold">로그인</h1>
      <form onSubmit={handleSubmit} className="mt-6 space-y-4">
        <div>
          <label className="label">이메일</label>
          <input
            type="email"
            className="input"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <div>
          <label className="label">비밀번호</label>
          <input
            type="password"
            className="input"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        {error && <p className="text-sm text-brand-600">{error}</p>}
        <button type="submit" className="btn-primary w-full" disabled={loading}>
          {loading ? '로그인 중...' : '로그인'}
        </button>
      </form>
      <p className="mt-4 text-center text-sm text-gray-600">
        계정이 없으신가요?{' '}
        <Link to="/register" className="text-brand-600 hover:underline">
          회원가입
        </Link>
      </p>
    </div>
  );
}