import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <div className="py-20 text-center">
      <h1 className="text-4xl font-bold text-gray-900">404</h1>
      <p className="mt-2 text-gray-600">페이지를 찾을 수 없습니다.</p>
      <Link to="/" className="btn-primary mt-6 inline-block">
        홈으로
      </Link>
    </div>
  );
}