import React, { useState, useEffect, useCallback } from 'react';
import Header from './components/Header';
import ConfessionForm from './components/ConfessionForm';
import ConfessionList from './components/ConfessionList';
import Ambient3DBackground from './components/3d/Ambient3DBackground';
import { getConfessions } from './services/api';
import { useRealtimeFeed } from './hooks/useRealtimeFeed';
import { AlertTriangle } from 'lucide-react';

export default function App() {
  const [confessions, setConfessions] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState(null);

  const fetchConfessionsData = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage(null);

    try {
      const data = await getConfessions();
      if (Array.isArray(data)) {
        setConfessions(data);
      } else {
        console.warn('API returned non-array payload:', data);
        setConfessions([]);
      }
    } catch (err) {
      console.error('Lỗi khi tải danh sách thú tội:', err);
      setErrorMessage(
        err.response?.data?.message ||
        'Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng hoặc thử lại sau.'
      );
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchConfessionsData();
  }, [fetchConfessionsData]);

  // Unified new confession handler with deduplication (for form submit & SSE live stream)
  const handleNewConfession = useCallback((newConfession) => {
    if (!newConfession || !newConfession.id) return;
    setConfessions((prev) => {
      if (prev.some((c) => c.id === newConfession.id)) return prev;
      return [newConfession, ...prev];
    });
  }, []);

  // Card like update handler
  const handleLikeUpdate = useCallback((confessionId, newLikeCount) => {
    setConfessions((prev) =>
      prev.map((item) =>
        item.id === confessionId ? { ...item, likes: newLikeCount } : item
      )
    );
  }, []);

  // Reconnection reconciliation handler
  const handleRealtimeReconnect = useCallback(() => {
    fetchConfessionsData();
  }, [fetchConfessionsData]);

  const { connectionStatus } = useRealtimeFeed({
    onNewConfession: handleNewConfession,
    onLikeUpdate: handleLikeUpdate,
    onReconnect: handleRealtimeReconnect,
  });

  return (
    <div className="min-h-screen bg-slate-950 text-slate-200 flex flex-col font-sans relative overflow-x-hidden selection:bg-rose-950 selection:text-rose-300">
      <Ambient3DBackground />
      <Header totalCount={confessions.length} connectionStatus={connectionStatus} />

      <main className="flex-1 max-w-6xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8 relative z-10">
        <section aria-labelledby="form-heading">
          <h2 id="form-heading" className="sr-only">Biểu mẫu gửi lời thú tội</h2>
          <ConfessionForm onConfessionCreated={handleNewConfession} />
        </section>

        <div className="flex items-center justify-between gap-4 pt-4 border-t border-slate-800/80">
          <div>
            <h2 className="text-xl font-bold text-slate-100 flex items-center gap-2">
              Bức tường chia sẻ
              <span className="text-xs font-normal px-2.5 py-0.5 rounded-full bg-rose-950/60 text-rose-300 border border-rose-900/50 shadow-[0_0_8px_rgba(225,29,72,0.2)]">
                Mới nhất
              </span>
            </h2>
            <p className="text-xs text-slate-400 mt-0.5">
              Những tâm tư thật lòng trong màn đêm huyền bí &bull; Cập nhật tự động theo thời gian thực
            </p>
          </div>
        </div>

        {errorMessage && (
          <div className="p-4 rounded-2xl bg-amber-950/40 border border-amber-800/60 text-amber-300 text-sm flex items-center justify-between gap-3 shadow-sm">
            <div className="flex items-center gap-2.5">
              <AlertTriangle className="w-5 h-5 text-amber-400 flex-shrink-0" />
              <span>{errorMessage}</span>
            </div>
            <button
              type="button"
              onClick={() => fetchConfessionsData(true)}
              className="text-xs font-semibold underline text-amber-300 hover:text-amber-100 flex-shrink-0"
            >
              Thử lại
            </button>
          </div>
        )}

        <section aria-label="Danh sách các lời thú tội">
          <ConfessionList
            confessions={confessions}
            isLoading={isLoading}
            onLikeUpdate={handleLikeUpdate}
          />
        </section>
      </main>

      <footer className="bg-slate-950/80 backdrop-blur-sm border-t border-slate-900 py-6 text-center text-xs text-slate-500 mt-auto relative z-10">
        <div className="max-w-6xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-2">
          <span>&copy; 2026 Bức Tường Thú Tội (Confession Wall). Mọi thông tin đều được ẩn danh.</span>
          <span className="text-rose-400 font-medium">Dark Gothic Romance Edition &bull; React, Three.js & Tailwind CSS</span>
        </div>
      </footer>
    </div>
  );
}
