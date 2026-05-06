import { api } from './client';
import type { Coupon, CouponCreateRequest, UserCoupon } from '@/types/api';

export const couponsApi = {
  issue: (code: string) =>
    api
      .post<UserCoupon>('/v1/coupons/issue', { code })
      .then((r) => r.data),
  my: () => api.get<UserCoupon[]>('/v1/coupons/my').then((r) => r.data),
  listAll: () => api.get<Coupon[]>('/v1/coupons').then((r) => r.data),
  create: (req: CouponCreateRequest) =>
    api.post<void>('/v1/coupons', req).then((r) => r.data),
};
