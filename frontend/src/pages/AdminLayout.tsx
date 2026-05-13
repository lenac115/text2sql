import { NavLink, Outlet } from 'react-router-dom';

const tabs = [
  { to: '/admin/query', label: 'Text2SQL 쿼리' },
  { to: '/admin/timedeals', label: '타임딜' },
];

export default function AdminLayout() {
  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-2xl font-bold">⚙️ 관리자</h1>
        <p className="mt-1 text-sm text-gray-500">
          어드민 전용 도구 모음.
        </p>
      </header>

      <nav className="flex gap-1 border-b border-gray-200">
        {tabs.map((t) => (
          <NavLink
            key={t.to}
            to={t.to}
            end
            className={({ isActive }) =>
              `-mb-px border-b-2 px-4 py-2 text-sm font-medium transition ${
                isActive
                  ? 'border-brand-600 text-brand-600'
                  : 'border-transparent text-gray-500 hover:text-brand-600'
              }`
            }
          >
            {t.label}
          </NavLink>
        ))}
      </nav>

      <Outlet />
    </div>
  );
}