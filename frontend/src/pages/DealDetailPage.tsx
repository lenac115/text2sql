import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { timeDealsApi } from '@/api/timedeals';
import { usersApi } from '@/api/users';
import type { TimeDeal, AddressDto } from '@/types/api';
import { formatKrw, formatDateTime, timeRemaining } from '@/utils/format';
import { useAuthStore } from '@/store/auth';
import { extractErrorMessage } from '@/api/client';

export default function DealDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { accessToken } = useAuthStore();
  const [deal, setDeal] = useState<TimeDeal | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [defaultAddress, setDefaultAddress] = useState<AddressDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [, tick] = useState(0);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    timeDealsApi
      .get(Number(id))
      .then(setDeal)
      .catch(() => setDeal(null))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    if (!accessToken) return;
    usersApi
      .me()
      .then((me) => setDefaultAddress(me.defaultAddress))
      .catch(() => setDefaultAddress(null));
  }, [accessToken]);

  useEffect(() => {
    const t = setInterval(() => tick((v) => v + 1), 1000);
    return () => clearInterval(t);
  }, []);

  const handlePurchase = async () => {
    if (!accessToken) {
      navigate('/login', { state: { from: `/deals/${id}` } });
      return;
    }
    if (!deal) return;
    if (!defaultAddress) {
      alert('내 정보에서 기본 배송지를 먼저 등록해주세요.');
      navigate('/me');
      return;
    }
    setError(null);
    setSubmitting(true);
    try {
      const order = await timeDealsApi.purchase(deal.id, quantity);
      navigate(`/orders/${order.orderId}`, {
        state: { paymentSucceeded: false },
      });
    } catch (err) {
      setError(extractErrorMessage(err, '구매에 실패했습니다.'));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <p className="text-gray-500">불러오는 중...</p>;
  if (!deal) return <p className="text-gray-500">타임딜을 찾을 수 없습니다.</p>;

  const isActive = deal.status === 'ACTIVE';
  const target = deal.status === 'UPCOMING' ? deal.startAt : deal.endAt;
  const targetLabel = deal.status === 'UPCOMING' ? '시작까지' : '종료까지';

  return (
    <div className="mx-auto max-w-3xl">
      <div className="card p-6">
        <h1 className="text-2xl font-bold">{deal.title}</h1>
        <p className="mt-1 text-gray-600">{deal.productName}</p>

        <div className="mt-4 flex items-baseline gap-3">
          <span className="text-3xl font-bold text-brand-600">
            {formatKrw(deal.dealPrice)}
          </span>
          <span className="text-lg text-gray-400 line-through">
            {formatKrw(deal.originalPrice)}
          </span>
          <span className="ml-auto rounded bg-brand-50 px-2 py-1 text-lg font-bold text-brand-600">
            {deal.discountRate}% 할인
          </span>
        </div>

        <div className="mt-6 grid grid-cols-2 gap-4 text-sm">
          <div>
            <p className="text-gray-500">상태</p>
            <p className="font-medium">{deal.status}</p>
          </div>
          <div>
            <p className="text-gray-500">{targetLabel}</p>
            <p className="font-medium">
              {deal.status === 'ENDED' || deal.status === 'SOLD_OUT'
                ? '-'
                : timeRemaining(target)}
            </p>
          </div>
          <div>
            <p className="text-gray-500">시작</p>
            <p>{formatDateTime(deal.startAt)}</p>
          </div>
          <div>
            <p className="text-gray-500">종료</p>
            <p>{formatDateTime(deal.endAt)}</p>
          </div>
          <div>
            <p className="text-gray-500">남은 수량</p>
            <p className="font-medium">
              {deal.remainingStock} / {deal.totalStock}
            </p>
          </div>
          <div>
            <p className="text-gray-500">1인 최대</p>
            <p>{deal.maxPerUser === 0 ? '제한 없음' : `${deal.maxPerUser}개`}</p>
          </div>
        </div>

        {isActive && (
          <div className="mt-6 border-t pt-6">
            <div className="flex items-center gap-3">
              <label className="text-sm font-medium">수량</label>
              <input
                type="number"
                min={1}
                max={
                  deal.maxPerUser > 0
                    ? Math.min(deal.maxPerUser, deal.remainingStock)
                    : deal.remainingStock
                }
                value={quantity}
                onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}
                className="input w-24"
              />
            </div>
            {error && <p className="mt-3 text-sm text-brand-600">{error}</p>}
            <button
              onClick={handlePurchase}
              disabled={submitting || deal.remainingStock <= 0}
              className="btn-primary mt-4 w-full"
            >
              {submitting ? '구매 중...' : `${formatKrw(deal.dealPrice * quantity)} 구매하기`}
            </button>
            <p className="mt-2 text-center text-xs text-gray-500">
              구매 후 주문상세 페이지에서 결제를 진행하세요.
            </p>
          </div>
        )}
      </div>
    </div>
  );
}