import { api } from './client';
import type { QueryResponse } from '@/types/api';

export const queryApi = {
  process: (question: string) =>
    api
      .post<QueryResponse>('/v1/query/process', { question })
      .then((r) => r.data),
};