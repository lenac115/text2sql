import { api } from './client';
import type { AddressDto, Order, TimeDeal } from '@/types/api';

export const timeDealsApi = {
  active: () =>
    api.get<TimeDeal[]>('/v1/deals/active').then((r) => r.data),
  upcoming: () =>
    api.get<TimeDeal[]>('/v1/deals/upcoming').then((r) => r.data),
  get: (id: number) =>
    api.get<TimeDeal>(`/v1/deals/${id}`).then((r) => r.data),
  purchase: (id: number, quantity: number, shippingAddress?: AddressDto | null) =>
    api
      .post<Order>(`/v1/deals/${id}/purchase`, { quantity, shippingAddress })
      .then((r) => r.data),
};