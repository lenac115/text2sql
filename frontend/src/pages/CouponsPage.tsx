import { FormEvent, useEffect, useState } from 'react';
import { couponsApi } from '@/api/coupons';
import type { UserCoupon } from '@/types/api';
import { formatKrw, formatDateTime } from '@/utils/format';
import { extractErrorMessage } from '@/api/client';

export default function CouponsPage() {
  const [coupons, setCoupons] = useState<UserCoupon[]>([]);
  const [loading, setLoading] = useState(true);
  const [code, setCode] = useState('');
  const [issueMsg, setIssueMsg] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const load = () => {
    setLoading(true);
    couponsApi
      .my()
      .then(setCoupons)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, []);

  const handleIssue = async (e: FormEvent) => {
    e.preventDefault();
    if (!code.trim()) return;
    setIssueMsg(null);
    setSubmitting(true);
    try {
      await couponsApi.issue(code.trim());
      setIssueMsg('쿠폰이 발급되었습니다.');
      setCode('');
      load();
    } catch (err) {
      setIssueMsg(extractErrorMessage(err, '쿠폰 발급에 실패했습니다.'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-8">
      <section>
        <h1 className="text-2xl font-bold">쿠폰</h1>
        <form onSubmit={handleIssue} className="mt-4 flex gap-2">
          <input
            className="input"
            placeholder="쿠폰 코드 입력"
            value={code}
            onChange={(e) => setCode(e.target.value)}
          />
          <button
            type="submit"
            disabled={submitting}
            className="btn-primary whitespace-nowrap"
          >
            발급받기
          </button>
        </form>
        {issueMsg && <p className="mt-2 text-sm text-gray-700">{issueMsg}</p>}
      </section>

      <section>
        <h2 className="mb-3 text-xl font-bold">내 쿠폰</h2>
        {loading ? (
          <p className="text-gray-500">불러오는 중...</p>
        ) : coupons.length === 0 ? (
          <p className="text-gray-500">보유한 쿠폰이 없습니다.</p>
        ) : (
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            {coupons.map((c) => (
              <div key={c.id} className={`card p-4 ${c.used ? 'opacity-60' : ''}`}>
                <div className="flex items-start justify-between">
                  <div>
                    <p className="text-sm text-gray-500">{c.code}</p>
                    <h3 className="font-semibold">{c.couponName}</h3>
                  </div>
                  {c.used && (
                    <span className="badge bg-gray-200 text-gray-700">사용됨</span>
                  )}
                </div>
                <p className="mt-2 text-lg font-bold text-brand-600">
                  {c.discountType === 'FIXED'
                    ? `${formatKrw(c.discountValue)} 할인`
                    : `${c.discountValue}% 할인${c.maxDiscountAmount > 0 ? ` (최대 ${formatKrw(c.maxDiscountAmount)})` : ''}`}
                </p>
                <p className="mt-1 text-xs text-gray-500">
                  최소 주문 {formatKrw(c.minOrderAmount)} · 만료{' '}
                  {formatDateTime(c.expiresAt)}
                </p>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}