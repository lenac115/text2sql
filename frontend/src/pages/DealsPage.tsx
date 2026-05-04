import { useEffect, useState } from 'react';
import { timeDealsApi } from '@/api/timedeals';
import type { TimeDeal } from '@/types/api';
import DealCard from '@/components/DealCard';

export default function DealsPage() {
  const [active, setActive] = useState<TimeDeal[]>([]);
  const [upcoming, setUpcoming] = useState<TimeDeal[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      timeDealsApi.active().catch(() => []),
      timeDealsApi.upcoming().catch(() => []),
    ]).then(([a, u]) => {
      setActive(a);
      setUpcoming(u);
      setLoading(false);
    });
  }, []);

  if (loading) return <p className="text-gray-500">불러오는 중...</p>;

  return (
    <div className="space-y-10">
      <section>
        <h1 className="mb-4 text-2xl font-bold">🔥 진행중인 타임딜</h1>
        {active.length === 0 ? (
          <p className="text-gray-500">진행중인 타임딜이 없습니다.</p>
        ) : (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {active.map((d) => (
              <DealCard key={d.id} deal={d} />
            ))}
          </div>
        )}
      </section>

      <section>
        <h2 className="mb-4 text-xl font-bold">⏰ 예정된 타임딜</h2>
        {upcoming.length === 0 ? (
          <p className="text-gray-500">예정된 타임딜이 없습니다.</p>
        ) : (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {upcoming.map((d) => (
              <DealCard key={d.id} deal={d} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}