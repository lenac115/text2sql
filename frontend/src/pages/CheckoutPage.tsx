import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ordersApi } from '@/api/orders';
import { paymentsApi } from '@/api/payments';
import { usersApi } from '@/api/users';
import { couponsApi } from '@/api/coupons';
import { cartApi } from '@/api/cart';
import { productsApi } from '@/api/products';
import type {
  AddressDto,
  PaymentMethod,
  Product,
  UserCoupon,
} from '@/types/api';
import { formatKrw } from '@/utils/format';
import { extractErrorMessage } from '@/api/client';

interface CheckoutItem {
  productId: number;
  quantity: number;
}

const PAYMENT_METHODS: { value: PaymentMethod; label: string }[] = [
  { value: 'CARD', label: '신용카드' },
  { value: 'KAKAO_PAY', label: '카카오페이' },
  { value: 'NAVER_PAY', label: '네이버페이' },
  { value: 'BANK_TRANSFER', label: '계좌이체' },
];

export default function CheckoutPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const state = location.state as
    | { items?: CheckoutItem[]; fromCart?: boolean }
    | null;
  const items = useMemo(() => state?.items ?? [], [state]);

  const [products, setProducts] = useState<Record<number, Product>>({});
  const [coupons, setCoupons] = useState<UserCoupon[]>([]);
  const [selectedCoupon, setSelectedCoupon] = useState<number | ''>('');
  const [address, setAddress] = useState<AddressDto>({
    recipient: '',
    phone: '',
    zipCode: '',
    addressLine1: '',
    addressLine2: '',
  });
  const [useDefaultAddress, setUseDefaultAddress] = useState(true);
  const [hasDefaultAddress, setHasDefaultAddress] = useState(false);
  const [method, setMethod] = useState<PaymentMethod>('CARD');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (items.length === 0) return;
    Promise.all(items.map((i) => productsApi.get(i.productId))).then((list) => {
      const map: Record<number, Product> = {};
      list.forEach((p) => (map[p.id] = p));
      setProducts(map);
    });
  }, [items]);

  useEffect(() => {
    couponsApi.my().then((all) => {
      setCoupons(all.filter((c) => !c.used));
    });
    usersApi.me().then((me) => {
      if (me.defaultAddress) {
        setHasDefaultAddress(true);
        setAddress(me.defaultAddress);
      } else {
        setUseDefaultAddress(false);
      }
    });
  }, []);

  const totalAmount = useMemo(() => {
    return items.reduce((sum, i) => {
      const p = products[i.productId];
      return p ? sum + p.price * i.quantity : sum;
    }, 0);
  }, [items, products]);

  const discountAmount = useMemo(() => {
    if (!selectedCoupon) return 0;
    const c = coupons.find((x) => x.id === selectedCoupon);
    if (!c) return 0;
    if (totalAmount < c.minOrderAmount) return 0;
    if (c.discountType === 'FIXED') return Math.min(c.discountValue, totalAmount);
    let d = Math.floor((totalAmount * c.discountValue) / 100);
    if (c.maxDiscountAmount > 0) d = Math.min(d, c.maxDiscountAmount);
    return d;
  }, [selectedCoupon, coupons, totalAmount]);

  const finalAmount = Math.max(0, totalAmount - discountAmount);

  const handleSubmit = async () => {
    if (items.length === 0) return;
    setError(null);
    setSubmitting(true);
    try {
      const order = await ordersApi.create({
        items,
        userCouponId: selectedCoupon || null,
        shippingAddress: useDefaultAddress ? null : address,
      });
      // Trigger payment
      const payment = await paymentsApi.pay(order.orderId, method);
      if (state?.fromCart) {
        try {
          await cartApi.clear();
        } catch {
          /* ignore */
        }
      }
      if (payment.status === 'SUCCEEDED') {
        navigate(`/orders/${order.orderId}`, {
          state: { paymentSucceeded: true },
        });
      } else {
        setError(
          `결제 실패: ${payment.failureReason ?? '알 수 없는 사유'}. 주문이 자동 취소되었습니다.`,
        );
      }
    } catch (err) {
      setError(extractErrorMessage(err, '주문/결제에 실패했습니다.'));
    } finally {
      setSubmitting(false);
    }
  };

  if (items.length === 0) {
    return (
      <div className="py-20 text-center">
        <p className="text-gray-500">주문할 상품이 없습니다.</p>
        <button onClick={() => navigate('/products')} className="btn-primary mt-4">
          상품 보러가기
        </button>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-[1fr_320px]">
      <div className="space-y-6">
        <section className="card p-4">
          <h2 className="mb-3 text-lg font-bold">주문 상품</h2>
          {items.map((i) => {
            const p = products[i.productId];
            if (!p) return <p key={i.productId}>로딩 중...</p>;
            return (
              <div
                key={i.productId}
                className="flex items-center justify-between border-b py-2 last:border-b-0"
              >
                <div>
                  <p className="font-medium">{p.name}</p>
                  <p className="text-sm text-gray-500">
                    {formatKrw(p.price)} × {i.quantity}
                  </p>
                </div>
                <p className="font-bold">{formatKrw(p.price * i.quantity)}</p>
              </div>
            );
          })}
        </section>

        <section className="card p-4">
          <h2 className="mb-3 text-lg font-bold">배송지</h2>
          {hasDefaultAddress && (
            <label className="mb-3 flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                checked={useDefaultAddress}
                onChange={(e) => setUseDefaultAddress(e.target.checked)}
              />
              기본 배송지 사용
            </label>
          )}
          <div
            className={`grid grid-cols-1 gap-3 sm:grid-cols-2 ${useDefaultAddress ? 'pointer-events-none opacity-60' : ''}`}
          >
            <div>
              <label className="label">받는 사람</label>
              <input
                className="input"
                value={address.recipient}
                onChange={(e) =>
                  setAddress({ ...address, recipient: e.target.value })
                }
              />
            </div>
            <div>
              <label className="label">연락처</label>
              <input
                className="input"
                value={address.phone}
                onChange={(e) =>
                  setAddress({ ...address, phone: e.target.value })
                }
              />
            </div>
            <div>
              <label className="label">우편번호</label>
              <input
                className="input"
                value={address.zipCode}
                onChange={(e) =>
                  setAddress({ ...address, zipCode: e.target.value })
                }
              />
            </div>
            <div>
              <label className="label">주소</label>
              <input
                className="input"
                value={address.addressLine1}
                onChange={(e) =>
                  setAddress({ ...address, addressLine1: e.target.value })
                }
              />
            </div>
            <div className="sm:col-span-2">
              <label className="label">상세주소</label>
              <input
                className="input"
                value={address.addressLine2 ?? ''}
                onChange={(e) =>
                  setAddress({ ...address, addressLine2: e.target.value })
                }
              />
            </div>
          </div>
        </section>

        <section className="card p-4">
          <h2 className="mb-3 text-lg font-bold">쿠폰</h2>
          <select
            className="input"
            value={selectedCoupon}
            onChange={(e) =>
              setSelectedCoupon(e.target.value ? Number(e.target.value) : '')
            }
          >
            <option value="">쿠폰 사용 안함</option>
            {coupons.map((c) => (
              <option key={c.id} value={c.id}>
                {c.couponName}{' '}
                {c.discountType === 'FIXED'
                  ? `(-${formatKrw(c.discountValue)})`
                  : `(-${c.discountValue}%)`}
              </option>
            ))}
          </select>
        </section>

        <section className="card p-4">
          <h2 className="mb-3 text-lg font-bold">결제 수단</h2>
          <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
            {PAYMENT_METHODS.map((m) => (
              <label
                key={m.value}
                className={`cursor-pointer rounded-md border px-3 py-2 text-center text-sm ${
                  method === m.value
                    ? 'border-brand-600 bg-brand-50 text-brand-700'
                    : 'border-gray-300 hover:bg-gray-50'
                }`}
              >
                <input
                  type="radio"
                  name="method"
                  value={m.value}
                  checked={method === m.value}
                  onChange={() => setMethod(m.value)}
                  className="hidden"
                />
                {m.label}
              </label>
            ))}
          </div>
          <p className="mt-2 text-xs text-gray-500">
            * 데모 환경 - 약 5% 확률로 결제 실패가 발생합니다.
          </p>
        </section>
      </div>

      <aside className="card sticky top-20 h-fit p-4">
        <h2 className="mb-3 text-lg font-bold">결제 요약</h2>
        <dl className="space-y-2 text-sm">
          <div className="flex justify-between">
            <dt>상품 합계</dt>
            <dd>{formatKrw(totalAmount)}</dd>
          </div>
          <div className="flex justify-between text-brand-600">
            <dt>할인</dt>
            <dd>-{formatKrw(discountAmount)}</dd>
          </div>
          <div className="flex justify-between border-t pt-2 text-lg font-bold">
            <dt>최종 결제</dt>
            <dd className="text-brand-600">{formatKrw(finalAmount)}</dd>
          </div>
        </dl>
        {error && <p className="mt-3 text-sm text-brand-600">{error}</p>}
        <button
          onClick={handleSubmit}
          disabled={submitting}
          className="btn-primary mt-4 w-full"
        >
          {submitting ? '결제 중...' : `${formatKrw(finalAmount)} 결제하기`}
        </button>
      </aside>
    </div>
  );
}