import React from 'react';
import { Heart, Sparkles, ShieldCheck } from 'lucide-react';

export default function Header({ totalCount = 0 }) {
  return (
    <header className="bg-white/80 backdrop-blur-md border-b border-rose-100 sticky top-0 z-30 transition-all shadow-sm">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
          {/* Brand & Titles */}
          <div className="flex items-center gap-3 text-center sm:text-left">
            <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-rose-500 to-pink-400 flex items-center justify-center shadow-md shadow-rose-500/20 transform hover:scale-105 transition-transform">
              <Heart className="w-6 h-6 text-white fill-white" />
            </div>
            <div>
              <div className="flex items-center gap-2 justify-center sm:justify-start">
                <h1 className="text-2xl sm:text-3xl font-bold bg-gradient-to-r from-rose-600 via-pink-600 to-rose-500 bg-clip-text text-transparent">
                  Bức Tường Thú Tội
                </h1>
                <span className="hidden sm:inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-0.5 rounded-full bg-rose-50 text-rose-600 border border-rose-200">
                  <Sparkles className="w-3 h-3 text-rose-500" />
                  Confession Wall
                </span>
              </div>
              <p className="text-sm text-slate-500 mt-0.5">
                Chia sẻ tâm tư thầm kín một cách ẩn danh
              </p>
            </div>
          </div>

          {/* Badges / Meta */}
          <div className="flex items-center gap-3">
            <div className="inline-flex items-center gap-1.5 text-xs text-emerald-700 bg-emerald-50 border border-emerald-200/70 px-3 py-1.5 rounded-full">
              <ShieldCheck className="w-3.5 h-3.5 text-emerald-500" />
              <span>100% Ẩn danh</span>
            </div>
            {totalCount > 0 && (
              <div className="inline-flex items-center gap-1 text-xs text-rose-700 bg-rose-50 border border-rose-200/70 px-3 py-1.5 rounded-full">
                <span className="font-semibold">{totalCount}</span>
                <span>lời thú tội</span>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}
