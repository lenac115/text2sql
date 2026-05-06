import { api } from './client';
import type { Category, CategoryCreateRequest } from '@/types/api';

export const categoriesApi = {
  list: () => api.get<Category[]>('/v1/categories').then((r) => r.data),
  create: (req: CategoryCreateRequest) =>
    api.post<Category>('/v1/categories', req).then((r) => r.data),
  update: (id: number, req: CategoryCreateRequest) =>
    api.put<Category>(`/v1/categories/${id}`, req).then((r) => r.data),
  remove: (id: number) =>
    api.delete<void>(`/v1/categories/${id}`).then((r) => r.data),
};
