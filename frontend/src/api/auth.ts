import { api } from './client';
import type { AuthResponse, LoginRequest, RegisterRequest } from '@/types/api';

export const authApi = {
  register: (req: RegisterRequest) =>
    api.post<AuthResponse>('/v1/auth/register', req).then((r) => r.data),
  login: (req: LoginRequest) =>
    api.post<AuthResponse>('/v1/auth/login', req).then((r) => r.data),
  logout: () => api.post<void>('/v1/auth/logout').then((r) => r.data),
};