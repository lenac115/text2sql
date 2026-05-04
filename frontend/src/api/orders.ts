import { api } from './client';
import type { Order, OrderCreateRequest } from '@/types/api';

export const ordersApi = {
  create: (req: OrderCreateRequest) =>
    api.post<Order>('/v1/orders', req).then((r) => r.data),
  list: () => api.get<Order[]>('/v1/orders').then((r) => r.data),
  get: (orderId: number) =>
    api.get<Order>(`/v1/orders/${orderId}`).then((r) => r.data),
  cancel: (orderId: number) =>
    api.post<Order>(`/v1/orders/${orderId}/cancel`).then((r) => r.data),
};