import { Outlet } from 'react-router-dom';
import Header from './Header';

export default function Layout() {
  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-6">
        <Outlet />
      </main>
      <footer className="border-t bg-white py-6 text-center text-xs text-gray-500">
        © {new Date().getFullYear()} HotDeal Market — Demo
      </footer>
    </div>
  );
}