import { api } from './client';
import type { Payment, PaymentMethod } from '@/types/api';

export const paymentsApi = {
  pay: (orderId: number, method: PaymentMethod) =>
    api
      .post<Payment>(`/v1/payments/${orderId}`, { method })
      .then((r) => r.data),
  get: (orderId: number) =>
    api.get<Payment>(`/v1/payments/${orderId}`).then((r) => r.data),
};