import { api } from './client';
import type { Category } from '@/types/api';

export const categoriesApi = {
  list: () => api.get<Category[]>('/v1/categories').then((r) => r.data),
};