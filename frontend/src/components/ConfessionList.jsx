import React from 'react';
import ConfessionCard from './ConfessionCard';
import { MessageSquarePlus } from 'lucide-react';

/**
 * Skeleton placeholder for loading state in Dark Gothic theme
 */
function SkeletonCard() {
  return (
    <div className="bg-slate-900/60 rounded-2xl p-5 border border-slate-800/60 shadow-card animate-pulse flex flex-col justify-between h-48">
      <div>
        <div className="flex items-center gap-3 mb-4">
          <div className="w-8 h-8 rounded-full bg-slate-800" />
          <div className="space-y-1.5 flex-1">
            <div className="h-3.5 bg-slate-800 rounded w-24" />
            <div className="h-2.5 bg-slate-800/60 rounded w-16" />
          </div>
        </div>
        <div className="space-y-2">
          <div className="h-3 bg-slate-800/70 rounded w-full" />
          <div className="h-3 bg-slate-800/70 rounded w-5/6" />
          <div className="h-3 bg-slate-800/70 rounded w-3/4" />
        </div>
      </div>
      <div className="pt-3 border-t border-slate-800/60 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="h-6 bg-slate-800 rounded-full w-20" />
          <div className="h-6 bg-slate-800 rounded-full w-20" />
        </div>
        <div className="h-3 bg-slate-800/60 rounded w-12" />
      </div>
    </div>
  );
}

export default function ConfessionList({ confessions = [], isLoading = false, onLikeUpdate, onOpenComments }) {
  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <SkeletonCard />
        <SkeletonCard />
        <SkeletonCard />
        <SkeletonCard />
        <SkeletonCard />
        <SkeletonCard />
      </div>
    );
  }

  if (!confessions || confessions.length === 0) {
    return (
      <div className="text-center py-16 px-4 bg-slate-900/60 backdrop-blur-md rounded-2xl border border-dashed border-rose-950/60 shadow-card">
        <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-rose-950/50 border border-rose-900/50 flex items-center justify-center text-rose-400 shadow-[0_0_15px_rgba(225,29,72,0.2)]">
          <MessageSquarePlus className="w-8 h-8" />
        </div>
        <h3 className="text-lg font-bold text-slate-200 mb-1">
          Chưa có lời thú tội nào
        </h3>
        <p className="text-sm text-slate-400 max-w-md mx-auto">
          Hãy là người đầu tiên chia sẻ tâm sự lên bức tường thú tội. Mọi điều bạn viết đều hoàn toàn ẩn danh!
        </p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 [perspective:1400px]">
      {confessions.map((item) => (
        <ConfessionCard
          key={item.id}
          confession={item}
          onLikeUpdate={onLikeUpdate}
          onOpenComments={onOpenComments}
        />
      ))}
    </div>
  );
}
