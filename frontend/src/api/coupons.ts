import { api } from './client';
import type { UserCoupon } from '@/types/api';

export const couponsApi = {
  issue: (code: string) =>
    api
      .post<UserCoupon>('/v1/coupons/issue', { code })
      .then((r) => r.data),
  my: () => api.get<UserCoupon[]>('/v1/coupons/my').then((r) => r.data),
};