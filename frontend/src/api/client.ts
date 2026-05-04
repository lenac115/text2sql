import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '@/store/auth';

export const api = axios.create({
  baseURL: '/',
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = useAuthStore.getState().accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let refreshing: Promise<string | null> | null = null;

async function refreshAccessToken(): Promise<string | null> {
  const refresh = useAuthStore.getState().refreshToken;
  if (!refresh) return null;
  try {
    const res = await axios.post('/v1/auth/refresh', { refreshToken: refresh });
    const { accessToken, refreshToken } = res.data as {
      accessToken: string;
      refreshToken: string;
    };
    useAuthStore.getState().setTokens(accessToken, refreshToken);
    return accessToken;
  } catch {
    useAuthStore.getState().clear();
    return null;
  }
}

api.interceptors.response.use(
  (r) => r,
  async (error: AxiosError) => {
    const original = error.config as
      | (InternalAxiosRequestConfig & { _retry?: boolean })
      | undefined;
    const status = error.response?.status;
    const url = original?.url ?? '';

    if (
      status === 401 &&
      original &&
      !original._retry &&
      !url.includes('/v1/auth/')
    ) {
      original._retry = true;
      if (!refreshing) refreshing = refreshAccessToken();
      const newToken = await refreshing;
      refreshing = null;
      if (newToken) {
        original.headers = original.headers ?? {};
        (original.headers as Record<string, string>).Authorization =
          `Bearer ${newToken}`;
        return api.request(original);
      }
    }
    return Promise.reject(error);
  },
);

export function extractErrorMessage(err: unknown, fallback = '요청에 실패했습니다.') {
  if (axios.isAxiosError(err)) {
    const data = err.response?.data as { message?: string } | undefined;
    if (data?.message) return data.message;
    if (err.message) return err.message;
  }
  if (err instanceof Error) return err.message;
  return fallback;
}