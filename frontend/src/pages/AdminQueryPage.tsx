import { FormEvent, useState } from 'react';
import { queryApi } from '@/api/query';
import type { QueryResponse } from '@/types/api';
import { extractErrorMessage } from '@/api/client';

export default function AdminQueryPage() {
  const [question, setQuestion] = useState('');
  const [response, setResponse] = useState<QueryResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!question.trim()) return;
    setError(null);
    setResponse(null);
    setLoading(true);
    try {
      setResponse(await queryApi.process(question));
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-2xl font-bold">🛠 Text2SQL 쿼리</h1>
        <p className="mt-1 text-sm text-gray-500">
          자연어로 질문하면 SQL을 생성하고 실행 결과를 보여줍니다.
        </p>
      </header>

      <form onSubmit={handleSubmit} className="card space-y-3 p-4">
        <textarea
          className="input min-h-32"
          placeholder="예) 최근 7일 동안 가장 많이 팔린 상품 5개를 보여줘"
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
        />
        <button type="submit" disabled={loading} className="btn-primary">
          {loading ? '분석 중...' : '쿼리 실행'}
        </button>
      </form>

      {error && (
        <div className="rounded-md bg-red-50 p-3 text-sm text-red-800">{error}</div>
      )}

      {response && !response.success && response.error && (
        <div className="card p-4">
          <h3 className="font-bold text-brand-600">오류</h3>
          <p className="mt-1 text-sm">
            <span className="font-mono text-xs text-gray-500">
              {response.error.code}
            </span>
            : {response.error.message}
          </p>
          {response.error.detail && (
            <pre className="mt-2 max-h-64 overflow-auto rounded bg-gray-50 p-2 text-xs">
              {JSON.stringify(response.error.detail, null, 2)}
            </pre>
          )}
        </div>
      )}

      {response?.success && response.data && (
        <>
          <section className="card p-4">
            <h3 className="mb-2 font-bold">결과 요약</h3>
            <p className="whitespace-pre-line text-sm text-gray-700">
              {response.data.summary}
            </p>
            <p className="mt-2 text-xs text-gray-500">
              실행 {response.data.metadata.executionTimeMs}ms · 행{' '}
              {response.data.metadata.rowCount}개
              {response.data.metadata.cached && ' · 캐시됨'}
            </p>
          </section>

          <section className="card p-4">
            <h3 className="mb-2 font-bold">생성된 SQL</h3>
            <pre className="max-h-64 overflow-auto rounded bg-gray-900 p-3 text-xs text-green-300">
              {response.data.generatedSql}
            </pre>
          </section>

          <section className="card p-4">
            <h3 className="mb-2 font-bold">데이터</h3>
            {response.data.rows.length === 0 ? (
              <p className="text-sm text-gray-500">결과가 없습니다.</p>
            ) : (
              <div className="overflow-auto">
                <table className="w-full text-sm">
                  <thead className="bg-gray-50">
                    <tr>
                      {response.data.columns.map((c) => (
                        <th key={c} className="border-b px-3 py-2 text-left">
                          {c}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {response.data.rows.map((row, i) => (
                      <tr key={i} className="hover:bg-gray-50">
                        {response.data!.columns.map((c) => (
                          <td key={c} className="border-b px-3 py-2 align-top">
                            {row[c] == null ? (
                              <span className="text-gray-400">null</span>
                            ) : typeof row[c] === 'object' ? (
                              JSON.stringify(row[c])
                            ) : (
                              String(row[c])
                            )}
                          </td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </>
      )}
    </div>
  );
}