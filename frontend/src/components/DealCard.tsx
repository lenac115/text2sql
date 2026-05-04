import { Link } from 'react-router-dom';
import type { TimeDeal } from '@/types/api';
import { formatKrw, timeRemaining } from '@/utils/format';
import { useEffect, useState } from 'react';

const STATUS_BADGE: Record<TimeDeal['status'], string> = {
  ACTIVE: 'bg-brand-50 text-brand-700',
  UPCOMING: 'bg-blue-50 text-blue-700',
  SOLD_OUT: 'bg-gray-200 text-gray-700',
  ENDED: 'bg-gray-200 text-gray-500',
};

const STATUS_LABEL: Record<TimeDeal['status'], string> = {
  ACTIVE: '진행중',
  UPCOMING: '예정',
  SOLD_OUT: '품절',
  ENDED: '종료',
};

export default function DealCard({ deal }: { deal: TimeDeal }) {
  const [, tick] = useState(0);
  useEffect(() => {
    if (deal.status !== 'ACTIVE' && deal.status !== 'UPCOMING') return;
    const id = setInterval(() => tick((v) => v + 1), 1000);
    return () => clearInterval(id);
  }, [deal.status]);

  const target = deal.status === 'UPCOMING' ? deal.startAt : deal.endAt;
  const targetLabel = deal.status === 'UPCOMING' ? '시작까지' : '종료까지';

  const stockRatio =
    deal.totalStock > 0
      ? Math.max(0, Math.min(100, (deal.remainingStock / deal.totalStock) * 100))
      : 0;

  return (
    <Link to={`/deals/${deal.id}`} className="card block overflow-hidden hover:shadow-md">
      <div className="p-4">
        <div className="mb-2 flex items-center justify-between">
          <span className={`badge ${STATUS_BADGE[deal.status]}`}>
            {STATUS_LABEL[deal.status]}
          </span>
          {(deal.status === 'ACTIVE' || deal.status === 'UPCOMING') && (
            <span className="text-xs text-gray-600">
              {targetLabel} {timeRemaining(target)}
            </span>
          )}
        </div>
        <h3 className="line-clamp-1 text-base font-semibold text-gray-900">
          {deal.title}
        </h3>
        <p className="mt-1 line-clamp-1 text-sm text-gray-500">
          {deal.productName}
        </p>
        <div className="mt-3 flex items-baseline gap-2">
          <span className="text-xl font-bold text-brand-600">
            {formatKrw(deal.dealPrice)}
          </span>
          <span className="text-sm text-gray-400 line-through">
            {formatKrw(deal.originalPrice)}
          </span>
          <span className="ml-auto text-sm font-bold text-brand-600">
            {deal.discountRate}%
          </span>
        </div>
        <div className="mt-3">
          <div className="h-2 overflow-hidden rounded-full bg-gray-100">
            <div
              className="h-full bg-brand-500"
              style={{ width: `${100 - stockRatio}%` }}
            />
          </div>
          <p className="mt-1 text-xs text-gray-500">
            남은 수량 {deal.remainingStock} / {deal.totalStock}
          </p>
        </div>
      </div>
    </Link>
  );
}