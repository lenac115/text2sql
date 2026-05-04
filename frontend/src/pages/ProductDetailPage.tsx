import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { productsApi } from '@/api/products';
import { cartApi } from '@/api/cart';
import type { Product } from '@/types/api';
import { formatKrw, formatDateTime } from '@/utils/format';
import { useAuthStore } from '@/store/auth';
import { extractErrorMessage } from '@/api/client';

export default function ProductDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { accessToken } = useAuthStore();
  const [product, setProduct] = useState<Product | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    productsApi
      .get(Number(id))
      .then(setProduct)
      .catch(() => setProduct(null))
      .finally(() => setLoading(false));
  }, [id]);

  const handleAddToCart = async () => {
    if (!accessToken) {
      navigate('/login', { state: { from: `/products/${id}` } });
      return;
    }
    if (!product) return;
    setMessage(null);
    setSubmitting(true);
    try {
      await cartApi.add(product.id, quantity);
      setMessage('장바구니에 담았습니다.');
    } catch (err) {
      setMessage(extractErrorMessage(err, '장바구니 담기에 실패했습니다.'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleBuyNow = async () => {
    if (!accessToken) {
      navigate('/login', { state: { from: `/products/${id}` } });
      return;
    }
    if (!product) return;
    navigate('/checkout', {
      state: { items: [{ productId: product.id, quantity }] },
    });
  };

  if (loading) return <p className="text-gray-500">불러오는 중...</p>;
  if (!product) return <p className="text-gray-500">상품을 찾을 수 없습니다.</p>;

  return (
    <div className="grid grid-cols-1 gap-8 md:grid-cols-2">
      <div className="aspect-square w-full bg-gray-100">
        {product.imageUrl ? (
          <img
            src={product.imageUrl}
            alt={product.name}
            className="h-full w-full object-cover"
          />
        ) : (
          <div className="flex h-full items-center justify-center text-gray-400">
            No Image
          </div>
        )}
      </div>
      <div>
        {product.categoryName && (
          <p className="text-sm text-gray-500">{product.categoryName}</p>
        )}
        <h1 className="mt-1 text-2xl font-bold text-gray-900">{product.name}</h1>
        <p className="mt-3 text-3xl font-bold text-brand-600">
          {formatKrw(product.price)}
        </p>
        <p className="mt-2 text-sm text-gray-500">
          재고: {product.stock} · 등록 {formatDateTime(product.createdAt)}
        </p>
        {product.description && (
          <p className="mt-4 whitespace-pre-line text-sm text-gray-700">
            {product.description}
          </p>
        )}
        <div className="mt-6 flex items-center gap-3">
          <label className="text-sm font-medium">수량</label>
          <input
            type="number"
            min={1}
            max={product.stock || 99}
            value={quantity}
            onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}
            className="input w-24"
          />
        </div>
        <div className="mt-6 flex gap-3">
          <button
            onClick={handleAddToCart}
            disabled={submitting || product.stock <= 0}
            className="btn-outline flex-1"
          >
            장바구니 담기
          </button>
          <button
            onClick={handleBuyNow}
            disabled={submitting || product.stock <= 0}
            className="btn-primary flex-1"
          >
            바로 구매
          </button>
        </div>
        {message && (
          <p className="mt-3 text-sm text-gray-700">{message}</p>
        )}
      </div>
    </div>
  );
}