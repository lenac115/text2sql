import { api } from './client';
import type { AddressDto, UserProfile } from '@/types/api';

export const usersApi = {
  me: () => api.get<UserProfile>('/v1/users/me').then((r) => r.data),
  updateProfile: (name: string) =>
    api.put<UserProfile>('/v1/users/me', { name }).then((r) => r.data),
  updatePassword: (currentPassword: string, newPassword: string) =>
    api
      .put<void>('/v1/users/me/password', { currentPassword, newPassword })
      .then((r) => r.data),
  updateAddress: (addr: AddressDto) =>
    api
      .put<UserProfile>('/v1/users/me/address', addr)
      .then((r) => r.data),
};