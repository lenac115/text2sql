import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/auth';
import { authApi } from '@/api/auth';

export default function Header() {
  const { accessToken, name, role, clear } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await authApi.logout();
    } catch {
      /* ignore */
    }
    clear();
    navigate('/login');
  };

  const navItem = ({ isActive }: { isActive: boolean }) =>
    `px-3 py-2 text-sm font-medium ${
      isActive ? 'text-brand-600' : 'text-gray-700 hover:text-brand-600'
    }`;

  return (
    <header className="sticky top-0 z-10 border-b bg-white">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
        <Link to="/" className="text-xl font-bold text-brand-600">
          🔥 HotDeal
        </Link>
        <nav className="flex items-center gap-1">
          <NavLink to="/" end className={navItem}>
            홈
          </NavLink>
          <NavLink to="/products" className={navItem}>
            상품
          </NavLink>
          <NavLink to="/deals" className={navItem}>
            타임딜
          </NavLink>
          {accessToken && (
            <>
              <NavLink to="/cart" className={navItem}>
                장바구니
              </NavLink>
              <NavLink to="/orders" className={navItem}>
                주문내역
              </NavLink>
              <NavLink to="/coupons" className={navItem}>
                쿠폰
              </NavLink>
              <NavLink to="/me" className={navItem}>
                내 정보
              </NavLink>
            </>
          )}
          {role === 'ADMIN' && (
            <NavLink to="/admin/query" className={navItem}>
              관리자
            </NavLink>
          )}
        </nav>
        <div className="flex items-center gap-2">
          {accessToken ? (
            <>
              <span className="text-sm text-gray-600">{name}님</span>
              <button onClick={handleLogout} className="btn-outline">
                로그아웃
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn-outline">
                로그인
              </Link>
              <Link to="/register" className="btn-primary">
                회원가입
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}