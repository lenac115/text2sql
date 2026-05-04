import { useEffect, useState } from 'react';
import { useLocation, useParams } from 'react-router-dom';
import { ordersApi } from '@/api/orders';
import { paymentsApi } from '@/api/payments';
import type { Order, OrderStatus, Payment } from '@/types/api';
import { formatKrw, formatDateTime } from '@/utils/format';
import { extractErrorMessage } from '@/api/client';

const STATUS_LABEL: Record<OrderStatus, string> = {
  PAYMENT_PENDING: '결제 대기',
  PAID: '결제완료',
  SHIPPING: '배송중',
  DELIVERED: '배송완료',
  CANCELLED: '취소됨',
  REFUNDED: '환불됨',
};

export default function OrderDetailPage() {
  const { id } = useParams<{ id: string }>();
  const location = useLocation();
  const paymentSucceeded =
    (location.state as { paymentSucceeded?: boolean } | null)?.paymentSucceeded ??
    false;

  const [order, setOrder] = useState<Order | null>(null);
  const [payment, setPayment] = useState<Payment | null>(null);
  const [loading, setLoading] = useState(true);
  const [cancelling, setCancelling] = useState(false);

  const load = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const [o, p] = await Promise.all([
        ordersApi.get(Number(id)),
        paymentsApi.get(Number(id)).catch(() => null),
      ]);
      setOrder(o);
      setPayment(p);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, [id]);

  const handleCancel = async () => {
    if (!order) return;
    if (!confirm('주문을 취소하시겠습니까?')) return;
    setCancelling(true);
    try {
      const updated = await ordersApi.cancel(order.orderId);
      setOrder(updated);
    } catch (err) {
      alert(extractErrorMessage(err, '주문 취소에 실패했습니다.'));
    } finally {
      setCancelling(false);
    }
  };

  if (loading) return <p className="text-gray-500">불러오는 중...</p>;
  if (!order) return <p className="text-gray-500">주문을 찾을 수 없습니다.</p>;

  const canCancel =
    order.status === 'PAYMENT_PENDING' || order.status === 'PAID';

  return (
    <div className="space-y-6">
      {paymentSucceeded && (
        <div className="rounded-md bg-green-50 p-4 text-sm text-green-800">
          ✅ 결제가 완료되었습니다.
        </div>
      )}
      <div>
        <h1 className="text-2xl font-bold">주문 #{order.orderId}</h1>
        <p className="mt-1 text-sm text-gray-500">
          {formatDateTime(order.orderedAt)}
        </p>
        <span className="badge mt-2 bg-brand-50 text-brand-700">
          {STATUS_LABEL[order.status]}
        </span>
      </div>

      <section className="card p-4">
        <h2 className="mb-3 text-lg font-bold">주문 상품</h2>
        {order.items.map((it) => (
          <div
            key={it.productId}
            className="flex items-center justify-between border-b py-2 last:border-b-0"
          >
            <div>
              <p className="font-medium">{it.productName}</p>
              <p className="text-sm text-gray-500">
                {formatKrw(it.unitPrice)} × {it.quantity}
              </p>
            </div>
            <p className="font-bold">{formatKrw(it.subtotal)}</p>
          </div>
        ))}
      </section>

      {order.shippingAddress && (
        <section className="card p-4">
          <h2 className="mb-3 text-lg font-bold">배송지</h2>
          <p className="font-medium">{order.shippingAddress.recipient}</p>
          <p className="text-sm text-gray-600">{order.shippingAddress.phone}</p>
          <p className="mt-1 text-sm">
            ({order.shippingAddress.zipCode}) {order.shippingAddress.addressLine1}{' '}
            {order.shippingAddress.addressLine2}
          </p>
        </section>
      )}

      {payment && (
        <section className="card p-4">
          <h2 className="mb-3 text-lg font-bold">결제 정보</h2>
          <dl className="space-y-1 text-sm">
            <div className="flex justify-between">
              <dt className="text-gray-500">결제 수단</dt>
              <dd>{payment.method}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-gray-500">결제 상태</dt>
              <dd>{payment.status}</dd>
            </div>
            {payment.pgTransactionId && (
              <div className="flex justify-between">
                <dt className="text-gray-500">거래 번호</dt>
                <dd className="font-mono text-xs">{payment.pgTransactionId}</dd>
              </div>
            )}
            {payment.failureReason && (
              <div className="flex justify-between">
                <dt className="text-gray-500">실패 사유</dt>
                <dd className="text-brand-600">{payment.failureReason}</dd>
              </div>
            )}
            <div className="flex justify-between">
              <dt className="text-gray-500">요청 시각</dt>
              <dd>{formatDateTime(payment.requestedAt)}</dd>
            </div>
          </dl>
        </section>
      )}

      <section className="card p-4">
        <dl className="space-y-2 text-sm">
          <div className="flex justify-between">
            <dt>상품 합계</dt>
            <dd>{formatKrw(order.totalAmount)}</dd>
          </div>
          <div className="flex justify-between text-brand-600">
            <dt>할인</dt>
            <dd>-{formatKrw(order.discountAmount)}</dd>
          </div>
          <div className="flex justify-between border-t pt-2 text-lg font-bold">
            <dt>최종 결제</dt>
            <dd className="text-brand-600">{formatKrw(order.finalAmount)}</dd>
          </div>
        </dl>
      </section>

      {canCancel && (
        <button
          onClick={handleCancel}
          disabled={cancelling}
          className="btn-outline"
        >
          {cancelling ? '취소 중...' : '주문 취소'}
        </button>
      )}
    </div>
  );
}