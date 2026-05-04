import { api } from './client';
import type { Cart, CartItem } from '@/types/api';

export const cartApi = {
  get: () => api.get<Cart>('/v1/cart').then((r) => r.data),
  add: (productId: number, quantity: number) =>
    api
      .post<CartItem>('/v1/cart', { productId, quantity })
      .then((r) => r.data),
  update: (itemId: number, quantity: number) =>
    api
      .patch<CartItem>(`/v1/cart/${itemId}`, { quantity })
      .then((r) => r.data),
  remove: (itemId: number) =>
    api.delete<void>(`/v1/cart/${itemId}`).then((r) => r.data),
  clear: () => api.delete<void>('/v1/cart').then((r) => r.data),
};