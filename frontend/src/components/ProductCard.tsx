import { Link } from 'react-router-dom';
import type { Product } from '@/types/api';
import { formatKrw } from '@/utils/format';

export default function ProductCard({ product }: { product: Product }) {
  return (
    <Link to={`/products/${product.id}`} className="card overflow-hidden hover:shadow-md">
      <div className="aspect-square w-full bg-gray-100">
        {product.imageUrl ? (
          <img
            src={product.imageUrl}
            alt={product.name}
            className="h-full w-full object-cover"
            onError={(e) => {
              (e.target as HTMLImageElement).style.display = 'none';
            }}
          />
        ) : (
          <div className="flex h-full items-center justify-center text-gray-400">
            No Image
          </div>
        )}
      </div>
      <div className="p-3">
        {product.categoryName && (
          <p className="text-xs text-gray-500">{product.categoryName}</p>
        )}
        <h3 className="mt-1 line-clamp-2 text-sm font-medium text-gray-900">
          {product.name}
        </h3>
        <p className="mt-2 text-base font-bold text-brand-600">
          {formatKrw(product.price)}
        </p>
        {product.stock <= 0 && (
          <span className="badge mt-1 bg-gray-200 text-gray-700">품절</span>
        )}
      </div>
    </Link>
  );
}