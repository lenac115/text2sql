import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '@/api/auth';
import { usersApi } from '@/api/users';
import { useAuthStore } from '@/store/auth';
import { extractErrorMessage } from '@/api/client';

export default function RegisterPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const { setTokens, setProfile } = useAuthStore();
  const navigate = useNavigate();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    if (password.length < 8) {
      setError('비밀번호는 8자 이상이어야 합니다.');
      return;
    }
    setLoading(true);
    try {
      const auth = await authApi.register({ email, password, name });
      setTokens(auth.accessToken, auth.refreshToken);
      try {
        const me = await usersApi.me();
        setProfile(me.email, me.name, me.role);
      } catch {
        setProfile(auth.email, auth.name);
      }
      navigate('/');
    } catch (err) {
      setError(extractErrorMessage(err, '회원가입에 실패했습니다.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto max-w-sm py-10">
      <h1 className="text-2xl font-bold">회원가입</h1>
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
          <label className="label">이름</label>
          <input
            type="text"
            className="input"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
        </div>
        <div>
          <label className="label">비밀번호 (8자 이상)</label>
          <input
            type="password"
            className="input"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            minLength={8}
            required
          />
        </div>
        {error && <p className="text-sm text-brand-600">{error}</p>}
        <button type="submit" className="btn-primary w-full" disabled={loading}>
          {loading ? '가입 중...' : '회원가입'}
        </button>
      </form>
      <p className="mt-4 text-center text-sm text-gray-600">
        이미 가입하셨나요?{' '}
        <Link to="/login" className="text-brand-600 hover:underline">
          로그인
        </Link>
      </p>
    </div>
  );
}