import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ordersApi } from '@/api/orders';
import type { Order, OrderStatus } from '@/types/api';
import { formatKrw, formatDateTime } from '@/utils/format';
import { useAuthStore } from '@/store/auth';

const STATUS_LABEL: Record<OrderStatus, string> = {
  PAYMENT_PENDING: '결제 대기',
  PAID: '결제완료',
  SHIPPING: '배송중',
  DELIVERED: '배송완료',
  CANCELLED: '취소됨',
  REFUNDED: '환불됨',
};

const STATUS_COLOR: Record<OrderStatus, string> = {
  PAYMENT_PENDING: 'bg-yellow-100 text-yellow-800',
  PAID: 'bg-blue-100 text-blue-800',
  SHIPPING: 'bg-indigo-100 text-indigo-800',
  DELIVERED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-gray-200 text-gray-700',
  REFUNDED: 'bg-gray-200 text-gray-700',
};

export default function OrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const { accessToken } = useAuthStore();

  const reload = () => {
    setLoading(true);
    ordersApi
      .list()
      .then(setOrders)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    reload();
  }, []);

  // SSE 구독: 주문 상태 변경 시 자동 갱신
  useEffect(() => {
    if (!accessToken) return;
    let cancelled = false;
    let controller: AbortController | null = null;

    const start = async () => {
      controller = new AbortController();
      try {
        const response = await fetch('/v1/orders/subscribe', {
          headers: { Authorization: `Bearer ${accessToken}` },
          signal: controller.signal,
        });
        if (!response.ok || !response.body) return;
        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';
        while (!cancelled) {
          const { done, value } = await reader.read();
          if (done) break;
          buffer += decoder.decode(value, { stream: true });
          const events = buffer.split('\n\n');
          buffer = events.pop() ?? '';
          for (const evt of events) {
            if (evt.includes('event:order-status') || evt.includes('data:')) {
              reload();
            }
          }
        }
      } catch {
        /* connection closed */
      }
    };

    start();
    return () => {
      cancelled = true;
      controller?.abort();
    };
  }, [accessToken]);

  if (loading) return <p className="text-gray-500">불러오는 중...</p>;
  if (orders.length === 0)
    return (
      <div className="py-20 text-center">
        <p className="text-gray-500">주문 내역이 없습니다.</p>
      </div>
    );

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold">주문 내역</h1>
      <div className="space-y-3">
        {orders.map((o) => (
          <Link
            key={o.orderId}
            to={`/orders/${o.orderId}`}
            className="card flex items-center justify-between p-4 hover:shadow-md"
          >
            <div>
              <div className="flex items-center gap-2">
                <span className="font-mono text-sm text-gray-500">
                  #{o.orderId}
                </span>
                <span className={`badge ${STATUS_COLOR[o.status]}`}>
                  {STATUS_LABEL[o.status]}
                </span>
              </div>
              <p className="mt-1 font-medium">
                {o.items[0]?.productName}
                {o.items.length > 1 && ` 외 ${o.items.length - 1}건`}
              </p>
              <p className="text-sm text-gray-500">
                {formatDateTime(o.orderedAt)}
              </p>
            </div>
            <p className="text-lg font-bold text-brand-600">
              {formatKrw(o.finalAmount)}
            </p>
          </Link>
        ))}
      </div>
    </div>
  );
}