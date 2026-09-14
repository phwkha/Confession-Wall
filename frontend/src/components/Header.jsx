import React from 'react';
import { Sparkles, ShieldCheck } from 'lucide-react';
import HeaderHeart3D from './3d/HeaderHeart3D';

export default function Header({ totalCount = 0 }) {
  return (
    <header className="bg-slate-950/80 backdrop-blur-md border-b border-purple-950/40 sticky top-0 z-30 transition-all shadow-md shadow-black/40">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-3 text-center sm:text-left">
            <HeaderHeart3D />
            <div>
              <div className="flex items-center gap-2 justify-center sm:justify-start">
                <h1 className="text-2xl sm:text-3xl font-bold bg-gradient-to-r from-purple-300 via-fuchsia-400 to-pink-400 bg-clip-text text-transparent drop-shadow-[0_0_14px_rgba(168,85,247,0.4)]">
                  Bức Tường Thú Tội
                </h1>
                <span className="hidden sm:inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-0.5 rounded-full bg-purple-950/60 text-purple-300 border border-purple-800/50 shadow-[0_0_8px_rgba(168,85,247,0.25)]">
                  <Sparkles className="w-3 h-3 text-purple-400" />
                  Confession Wall
                </span>
              </div>
              <p className="text-sm text-slate-400 mt-0.5">
                Chia sẻ tâm tư thầm kín một cách ẩn danh
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="inline-flex items-center gap-1.5 text-xs text-emerald-400 bg-emerald-950/50 border border-emerald-900/50 px-3 py-1.5 rounded-full">
              <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
              <span>100% Ẩn danh</span>
            </div>
            {totalCount > 0 && (
              <div className="inline-flex items-center gap-1 text-xs text-purple-300 bg-purple-950/60 border border-purple-800/60 px-3 py-1.5 rounded-full shadow-[0_0_10px_rgba(168,85,247,0.25)]">
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
