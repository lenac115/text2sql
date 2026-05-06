import { api } from './client';
import type {
  Page,
  Product,
  ProductCreateRequest,
  ProductUpdateRequest,
} from '@/types/api';

export interface ProductQuery {
  categoryId?: number | null;
  keyword?: string;
  page?: number;
  size?: number;
}

export const productsApi = {
  list: (q: ProductQuery = {}) => {
    const params: Record<string, string | number> = {
      page: q.page ?? 0,
      size: q.size ?? 20,
    };
    if (q.categoryId != null) params.categoryId = q.categoryId;
    if (q.keyword) params.keyword = q.keyword;
    return api.get<Page<Product>>('/v1/products', { params }).then((r) => r.data);
  },
  get: (id: number) =>
    api.get<Product>(`/v1/products/${id}`).then((r) => r.data),
  create: (req: ProductCreateRequest) =>
    api.post<Product>('/v1/products', req).then((r) => r.data),
  update: (id: number, req: ProductUpdateRequest) =>
    api.put<Product>(`/v1/products/${id}`, req).then((r) => r.data),
  remove: (id: number) =>
    api.delete<void>(`/v1/products/${id}`).then((r) => r.data),
};
