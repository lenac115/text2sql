import { FormEvent, useEffect, useState } from 'react';
import { timeDealsApi } from '@/api/timedeals';
import { productsApi } from '@/api/products';
import type { Product, TimeDeal } from '@/types/api';
import { extractErrorMessage } from '@/api/client';
import { formatDateTime, formatKrw } from '@/utils/format';

interface FormState {
  title: string;
  productId: string;
  dealPrice: string;
  totalStock: string;
  maxPerUser: string;
  startAt: string;
  endAt: string;
}

const initialForm: FormState = {
  title: '',
  productId: '',
  dealPrice: '',
  totalStock: '',
  maxPerUser: '0',
  startAt: '',
  endAt: '',
};

function toLocalDateTimeInput(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0');
  return (
    date.getFullYear() +
    '-' +
    pad(date.getMonth() + 1) +
    '-' +
    pad(date.getDate()) +
    'T' +
    pad(date.getHours()) +
    ':' +
    pad(date.getMinutes())
  );
}

export default function AdminTimeDealPage() {
  const [form, setForm] = useState<FormState>(() => {
    const now = new Date();
    const oneHourLater = new Date(now.getTime() + 60 * 60 * 1000);
    const oneDayLater = new Date(now.getTime() + 24 * 60 * 60 * 1000);
    return {
      ...initialForm,
      startAt: toLocalDateTimeInput(oneHourLater),
      endAt: toLocalDateTimeInput(oneDayLater),
    };
  });
  const [products, setProducts] = useState<Product[]>([]);
  const [deals, setDeals] = useState<TimeDeal[]>([]);
  const [productsLoading, setProductsLoading] = useState(true);
  const [dealsLoading, setDealsLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const loadDeals = async () => {
    setDealsLoading(true);
    try {
      const all = await timeDealsApi.listAll();
      setDeals(all);
    } catch (e) {
      console.error(e);
    } finally {
      setDealsLoading(false);
    }
  };

  useEffect(() => {
    (async () => {
      try {
        const page = await productsApi.list({ size: 100 });
        setProducts(page.content);
      } catch (e) {
        console.error(e);
      } finally {
        setProductsLoading(false);
      }
    })();
    loadDeals();
  }, []);

  const selectedProduct = products.find(
    (p) => String(p.id) === form.productId,
  );

  const update = (k: keyof FormState) => (v: string) =>
    setForm((f) => ({ ...f, [k]: v }));

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    if (!form.title.trim()) {
      setError('타이틀을 입력해주세요.');
      return;
    }
    if (!form.productId) {
      setError('상품을 선택해주세요.');
      return;
    }
    const dealPrice = Number(form.dealPrice);
    const totalStock = Number(form.totalStock);
    const maxPerUser = Number(form.maxPerUser);
    if (!Number.isFinite(dealPrice) || dealPrice <= 0) {
      setError('딜 가격은 0보다 커야 합니다.');
      return;
    }
    if (!Number.isFinite(totalStock) || totalStock < 1) {
      setError('총 수량은 1 이상이어야 합니다.');
      return;
    }
    if (!Number.isFinite(maxPerUser) || maxPerUser < 0) {
      setError('1인당 한도는 0 이상이어야 합니다.');
      return;
    }
    if (!form.startAt || !form.endAt) {
      setError('시작/종료 시간을 입력해주세요.');
      return;
    }
    if (new Date(form.endAt) <= new Date(form.startAt)) {
      setError('종료 시간은 시작 시간보다 이후여야 합니다.');
      return;
    }

    setSubmitting(true);
    try {
      const created = await timeDealsApi.create({
        title: form.title.trim(),
        productId: Number(form.productId),
        dealPrice,
        totalStock,
        maxPerUser,
        startAt: form.startAt,
        endAt: form.endAt,
      });
      setSuccess(`타임딜 "${created.title}" (id=${created.id}) 생성 완료`);
      setForm((f) => ({ ...f, title: '', dealPrice: '', totalStock: '' }));
      loadDeals();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const statusBadgeClass = (status: TimeDeal['status']) => {
    switch (status) {
      case 'ACTIVE':
        return 'badge bg-green-100 text-green-800';
      case 'UPCOMING':
        return 'badge bg-blue-100 text-blue-800';
      case 'SOLD_OUT':
        return 'badge bg-orange-100 text-orange-800';
      case 'ENDED':
      default:
        return 'badge bg-gray-200 text-gray-700';
    }
  };

  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-2xl font-bold">🛠 타임딜 관리</h1>
        <p className="mt-1 text-sm text-gray-500">
          한정 수량 타임딜을 등록하고 전체 목록(예정/진행중/종료)을 확인합니다.
        </p>
      </header>

      <form onSubmit={handleSubmit} className="card space-y-4 p-4">
        <h2 className="text-lg font-bold">타임딜 등록</h2>

        <div>
          <label className="label">타이틀</label>
          <input
            className="input"
            placeholder="예) 주말 한정 갤럭시 핫딜"
            value={form.title}
            onChange={(e) => update('title')(e.target.value)}
          />
        </div>

        <div>
          <label className="label">상품</label>
          <select
            className="input"
            value={form.productId}
            onChange={(e) => update('productId')(e.target.value)}
            disabled={productsLoading}
          >
            <option value="">
              {productsLoading ? '불러오는 중...' : '상품을 선택하세요'}
            </option>
            {products.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name} — 정가 {formatKrw(p.price)} (재고 {p.stock})
              </option>
            ))}
          </select>
          {selectedProduct && (
            <p className="mt-1 text-xs text-gray-500">
              정가 {formatKrw(selectedProduct.price)} · 재고{' '}
              {selectedProduct.stock}개 — 딜 가격은 정가보다 낮아야 하고, 딜 수량은
              재고를 초과할 수 없습니다.
            </p>
          )}
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <div>
            <label className="label">딜 가격 (원)</label>
            <input
              type="number"
              className="input"
              min={1}
              value={form.dealPrice}
              onChange={(e) => update('dealPrice')(e.target.value)}
              placeholder="예) 99000"
            />
          </div>
          <div>
            <label className="label">총 수량</label>
            <input
              type="number"
              className="input"
              min={1}
              value={form.totalStock}
              onChange={(e) => update('totalStock')(e.target.value)}
              placeholder="예) 100"
            />
          </div>
          <div>
            <label className="label">1인당 한도 (0=무제한)</label>
            <input
              type="number"
              className="input"
              min={0}
              value={form.maxPerUser}
              onChange={(e) => update('maxPerUser')(e.target.value)}
            />
          </div>
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="label">시작 시간</label>
            <input
              type="datetime-local"
              className="input"
              value={form.startAt}
              onChange={(e) => update('startAt')(e.target.value)}
            />
          </div>
          <div>
            <label className="label">종료 시간</label>
            <input
              type="datetime-local"
              className="input"
              value={form.endAt}
              onChange={(e) => update('endAt')(e.target.value)}
            />
          </div>
        </div>

        {error && (
          <div className="rounded-md bg-red-50 p-3 text-sm text-red-800">
            {error}
          </div>
        )}
        {success && (
          <div className="rounded-md bg-green-50 p-3 text-sm text-green-800">
            {success}
          </div>
        )}

        <div className="flex gap-2">
          <button
            type="submit"
            disabled={submitting}
            className="btn-primary"
          >
            {submitting ? '등록 중...' : '타임딜 등록'}
          </button>
          <button
            type="button"
            onClick={() => {
              setForm(initialForm);
              setError(null);
              setSuccess(null);
            }}
            className="btn-outline"
          >
            초기화
          </button>
        </div>
      </form>

      <section className="card p-4">
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-lg font-bold">전체 타임딜</h2>
          <button onClick={loadDeals} className="btn-outline" type="button">
            새로고침
          </button>
        </div>
        {dealsLoading ? (
          <p className="text-sm text-gray-500">불러오는 중...</p>
        ) : deals.length === 0 ? (
          <p className="text-sm text-gray-500">등록된 타임딜이 없습니다.</p>
        ) : (
          <div className="overflow-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50">
                <tr>
                  <th className="border-b px-3 py-2 text-left">ID</th>
                  <th className="border-b px-3 py-2 text-left">타이틀</th>
                  <th className="border-b px-3 py-2 text-left">상품</th>
                  <th className="border-b px-3 py-2 text-right">정가</th>
                  <th className="border-b px-3 py-2 text-right">딜가</th>
                  <th className="border-b px-3 py-2 text-right">할인</th>
                  <th className="border-b px-3 py-2 text-right">재고</th>
                  <th className="border-b px-3 py-2 text-left">시작</th>
                  <th className="border-b px-3 py-2 text-left">종료</th>
                  <th className="border-b px-3 py-2 text-left">상태</th>
                </tr>
              </thead>
              <tbody>
                {deals.map((d) => (
                  <tr key={d.id} className="hover:bg-gray-50">
                    <td className="border-b px-3 py-2">{d.id}</td>
                    <td className="border-b px-3 py-2">{d.title}</td>
                    <td className="border-b px-3 py-2">{d.productName}</td>
                    <td className="border-b px-3 py-2 text-right">
                      {formatKrw(d.originalPrice)}
                    </td>
                    <td className="border-b px-3 py-2 text-right font-medium text-brand-600">
                      {formatKrw(d.dealPrice)}
                    </td>
                    <td className="border-b px-3 py-2 text-right">
                      {d.discountRate}%
                    </td>
                    <td className="border-b px-3 py-2 text-right">
                      {d.remainingStock}/{d.totalStock}
                    </td>
                    <td className="border-b px-3 py-2">
                      {formatDateTime(d.startAt)}
                    </td>
                    <td className="border-b px-3 py-2">
                      {formatDateTime(d.endAt)}
                    </td>
                    <td className="border-b px-3 py-2">
                      <span className={statusBadgeClass(d.status)}>
                        {d.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}