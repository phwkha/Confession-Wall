import React, { useState, useEffect, useCallback } from 'react';
import Header from './components/Header';
import ConfessionForm from './components/ConfessionForm';
import ConfessionList from './components/ConfessionList';
import Ambient3DBackground from './components/3d/Ambient3DBackground';
import { getConfessions } from './services/api';
import { RefreshCw, AlertTriangle } from 'lucide-react';

export default function App() {
  const [confessions, setConfessions] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState(null);
  const [isRefreshing, setIsRefreshing] = useState(false);

  const fetchConfessionsData = useCallback(async (showRefreshingSpinner = false) => {
    if (showRefreshingSpinner) {
      setIsRefreshing(true);
    } else {
      setIsLoading(true);
    }
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
      setIsRefreshing(false);
    }
  }, []);

  useEffect(() => {
    fetchConfessionsData();
  }, [fetchConfessionsData]);

  const handleConfessionCreated = (newConfession) => {
    if (!newConfession) return;
    setConfessions((prev) => [newConfession, ...prev]);
  };

  const handleLikeUpdate = (confessionId, newLikeCount) => {
    setConfessions((prev) =>
      prev.map((item) =>
        item.id === confessionId ? { ...item, likes: newLikeCount } : item
      )
    );
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans relative overflow-x-hidden">
      <Ambient3DBackground />
      <Header totalCount={confessions.length} />

      <main className="flex-1 max-w-6xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8 relative z-10">
        <section aria-labelledby="form-heading">
          <h2 id="form-heading" className="sr-only">Biểu mẫu gửi lời thú tội</h2>
          <ConfessionForm onConfessionCreated={handleConfessionCreated} />
        </section>

        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pt-4 border-t border-slate-200/60">
          <div>
            <h2 className="text-xl font-bold text-slate-800 flex items-center gap-2">
              Bức tường chia sẻ
              <span className="text-xs font-normal px-2.5 py-0.5 rounded-full bg-slate-100 text-slate-600 border border-slate-200">
                Mới nhất
              </span>
            </h2>
            <p className="text-xs text-slate-500 mt-0.5">
              Những tâm tư thật lòng từ khắp mọi nơi
            </p>
          </div>

          <button
            type="button"
            onClick={() => fetchConfessionsData(true)}
            disabled={isLoading || isRefreshing}
            className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 text-xs font-medium shadow-sm transition-all disabled:opacity-60 disabled:cursor-not-allowed"
          >
            <RefreshCw className={`w-3.5 h-3.5 text-slate-500 ${isRefreshing ? 'animate-spin text-rose-500' : ''}`} />
            <span>Làm mới</span>
          </button>
        </div>

        {errorMessage && (
          <div className="p-4 rounded-2xl bg-amber-50 border border-amber-200 text-amber-800 text-sm flex items-center justify-between gap-3 shadow-sm">
            <div className="flex items-center gap-2.5">
              <AlertTriangle className="w-5 h-5 text-amber-600 flex-shrink-0" />
              <span>{errorMessage}</span>
            </div>
            <button
              type="button"
              onClick={() => fetchConfessionsData(true)}
              className="text-xs font-semibold underline text-amber-900 hover:text-amber-700 flex-shrink-0"
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

      <footer className="bg-white/80 backdrop-blur-sm border-t border-slate-200 py-6 text-center text-xs text-slate-400 mt-auto relative z-10">
        <div className="max-w-6xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-2">
          <span>&copy; 2026 Bức Tường Thú Tội (Confession Wall). Mọi thông tin đều được ẩn danh.</span>
          <span className="text-rose-500 font-medium">Được xây dựng với React, Vite & Tailwind CSS</span>
        </div>
      </footer>
    </div>
  );
}
