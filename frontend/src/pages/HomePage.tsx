import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { timeDealsApi } from '@/api/timedeals';
import { productsApi } from '@/api/products';
import type { Product, TimeDeal } from '@/types/api';
import DealCard from '@/components/DealCard';
import ProductCard from '@/components/ProductCard';

export default function HomePage() {
  const [activeDeals, setActiveDeals] = useState<TimeDeal[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      timeDealsApi.active().catch(() => []),
      productsApi.list({ size: 8 }).catch(() => ({ content: [] as Product[] })),
    ]).then(([deals, page]) => {
      setActiveDeals(deals);
      setProducts(page.content);
      setLoading(false);
    });
  }, []);

  return (
    <div className="space-y-10">
      <section className="rounded-xl bg-gradient-to-r from-brand-500 to-brand-700 p-8 text-white">
        <h1 className="text-3xl font-bold">놓치면 후회하는 핫딜</h1>
        <p className="mt-2 text-white/90">매일 한정 수량으로 진행되는 타임딜</p>
        <Link
          to="/deals"
          className="mt-4 inline-block rounded-md bg-white px-4 py-2 font-medium text-brand-700 hover:bg-gray-50"
        >
          타임딜 보러가기 →
        </Link>
      </section>

      <section>
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-xl font-bold">🔥 진행중인 타임딜</h2>
          <Link to="/deals" className="text-sm text-gray-600 hover:text-brand-600">
            전체보기 →
          </Link>
        </div>
        {loading ? (
          <p className="text-gray-500">불러오는 중...</p>
        ) : activeDeals.length === 0 ? (
          <p className="text-gray-500">현재 진행중인 타임딜이 없습니다.</p>
        ) : (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {activeDeals.slice(0, 3).map((d) => (
              <DealCard key={d.id} deal={d} />
            ))}
          </div>
        )}
      </section>

      <section>
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-xl font-bold">최신 상품</h2>
          <Link to="/products" className="text-sm text-gray-600 hover:text-brand-600">
            전체보기 →
          </Link>
        </div>
        {loading ? (
          <p className="text-gray-500">불러오는 중...</p>
        ) : (
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
            {products.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}