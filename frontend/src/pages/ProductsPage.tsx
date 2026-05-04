import { FormEvent, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { productsApi } from '@/api/products';
import { categoriesApi } from '@/api/categories';
import type { Category, Page, Product } from '@/types/api';
import ProductCard from '@/components/ProductCard';

export default function ProductsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = Number(searchParams.get('page') ?? 0);
  const keyword = searchParams.get('keyword') ?? '';
  const categoryIdStr = searchParams.get('categoryId');
  const categoryId = categoryIdStr ? Number(categoryIdStr) : null;

  const [categories, setCategories] = useState<Category[]>([]);
  const [data, setData] = useState<Page<Product> | null>(null);
  const [loading, setLoading] = useState(true);
  const [searchInput, setSearchInput] = useState(keyword);

  useEffect(() => {
    categoriesApi.list().then(setCategories).catch(() => setCategories([]));
  }, []);

  useEffect(() => {
    setLoading(true);
    productsApi
      .list({ page, keyword, categoryId, size: 20 })
      .then(setData)
      .finally(() => setLoading(false));
  }, [page, keyword, categoryId]);

  const updateParam = (next: Record<string, string | null>) => {
    const params = new URLSearchParams(searchParams);
    Object.entries(next).forEach(([k, v]) => {
      if (v == null || v === '') params.delete(k);
      else params.set(k, v);
    });
    setSearchParams(params);
  };

  const handleSearch = (e: FormEvent) => {
    e.preventDefault();
    updateParam({ keyword: searchInput, page: '0' });
  };

  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-[200px_1fr]">
      <aside className="space-y-2">
        <h2 className="font-semibold text-gray-900">카테고리</h2>
        <button
          onClick={() => updateParam({ categoryId: null, page: '0' })}
          className={`block w-full rounded px-3 py-2 text-left text-sm ${
            !categoryId ? 'bg-brand-50 text-brand-700' : 'hover:bg-gray-100'
          }`}
        >
          전체
        </button>
        <div className="max-h-96 space-y-1 overflow-y-auto">
          {categories.map((c) => (
            <button
              key={c.id}
              onClick={() => updateParam({ categoryId: String(c.id), page: '0' })}
              className={`block w-full rounded px-3 py-2 text-left text-sm ${
                categoryId === c.id
                  ? 'bg-brand-50 text-brand-700'
                  : 'hover:bg-gray-100'
              }`}
            >
              {c.name}{' '}
              <span className="text-xs text-gray-400">({c.productCount})</span>
            </button>
          ))}
        </div>
      </aside>

      <section>
        <form onSubmit={handleSearch} className="mb-4 flex gap-2">
          <input
            type="text"
            placeholder="상품명 검색"
            className="input"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
          />
          <button type="submit" className="btn-primary whitespace-nowrap">
            검색
          </button>
        </form>

        {loading ? (
          <p className="text-gray-500">불러오는 중...</p>
        ) : !data || data.content.length === 0 ? (
          <p className="text-gray-500">상품이 없습니다.</p>
        ) : (
          <>
            <p className="mb-3 text-sm text-gray-500">
              총 {data.totalElements}개
            </p>
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
              {data.content.map((p) => (
                <ProductCard key={p.id} product={p} />
              ))}
            </div>
            <div className="mt-6 flex justify-center gap-2">
              <button
                onClick={() => updateParam({ page: String(page - 1) })}
                disabled={data.first}
                className="btn-outline"
              >
                이전
              </button>
              <span className="flex items-center px-3 text-sm text-gray-600">
                {data.number + 1} / {data.totalPages}
              </span>
              <button
                onClick={() => updateParam({ page: String(page + 1) })}
                disabled={data.last}
                className="btn-outline"
              >
                다음
              </button>
            </div>
          </>
        )}
      </section>
    </div>
  );
}