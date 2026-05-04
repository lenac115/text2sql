import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/store/auth';

export default function ProtectedRoute({ adminOnly = false }: { adminOnly?: boolean }) {
  const { accessToken, role } = useAuthStore();
  const location = useLocation();

  if (!accessToken) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  if (adminOnly && role !== 'ADMIN') {
    return <Navigate to="/" replace />;
  }
  return <Outlet />;
}