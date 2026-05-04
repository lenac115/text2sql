import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { cartApi } from '@/api/cart';
import type { Cart } from '@/types/api';
import { formatKrw } from '@/utils/format';
import { extractErrorMessage } from '@/api/client';

export default function CartPage() {
  const navigate = useNavigate();
  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    setLoading(true);
    try {
      setCart(await cartApi.get());
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handleUpdate = async (itemId: number, quantity: number) => {
    if (quantity < 1) return;
    try {
      await cartApi.update(itemId, quantity);
      load();
    } catch (err) {
      alert(extractErrorMessage(err));
    }
  };

  const handleRemove = async (itemId: number) => {
    if (!confirm('이 상품을 장바구니에서 제거할까요?')) return;
    try {
      await cartApi.remove(itemId);
      load();
    } catch (err) {
      alert(extractErrorMessage(err));
    }
  };

  const handleCheckout = () => {
    if (!cart || cart.items.length === 0) return;
    navigate('/checkout', {
      state: {
        items: cart.items.map((i) => ({
          productId: i.productId,
          quantity: i.quantity,
        })),
        fromCart: true,
      },
    });
  };

  if (loading) return <p className="text-gray-500">불러오는 중...</p>;
  if (error) return <p className="text-brand-600">{error}</p>;
  if (!cart || cart.items.length === 0)
    return (
      <div className="py-20 text-center">
        <p className="text-gray-500">장바구니가 비어 있습니다.</p>
      </div>
    );

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold">장바구니</h1>
      <div className="space-y-3">
        {cart.items.map((item) => (
          <div
            key={item.cartItemId}
            className="card flex items-center justify-between gap-4 p-4"
          >
            <div className="flex-1">
              <h3 className="font-medium text-gray-900">{item.productName}</h3>
              <p className="text-sm text-gray-500">
                {formatKrw(item.unitPrice)} × {item.quantity}
              </p>
            </div>
            <input
              type="number"
              min={1}
              value={item.quantity}
              onChange={(e) =>
                handleUpdate(item.cartItemId, Number(e.target.value))
              }
              className="input w-20"
            />
            <p className="w-32 text-right font-bold text-brand-600">
              {formatKrw(item.subtotal)}
            </p>
            <button
              onClick={() => handleRemove(item.cartItemId)}
              className="btn-outline"
            >
              삭제
            </button>
          </div>
        ))}
      </div>
      <div className="mt-6 flex items-center justify-between border-t pt-4">
        <p className="text-lg">
          총 합계{' '}
          <span className="font-bold text-brand-600">
            {formatKrw(cart.totalAmount)}
          </span>
        </p>
        <button onClick={handleCheckout} className="btn-primary">
          주문하기
        </button>
      </div>
    </div>
  );
}