import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  email: string | null;
  name: string | null;
  role: 'USER' | 'ADMIN' | null;
  setTokens: (access: string, refresh: string) => void;
  setProfile: (email: string, name: string, role?: 'USER' | 'ADMIN') => void;
  clear: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      email: null,
      name: null,
      role: null,
      setTokens: (access, refresh) =>
        set({ accessToken: access, refreshToken: refresh }),
      setProfile: (email, name, role) => set({ email, name, role: role ?? null }),
      clear: () =>
        set({
          accessToken: null,
          refreshToken: null,
          email: null,
          name: null,
          role: null,
        }),
    }),
    { name: 'auth-storage' },
  ),
);