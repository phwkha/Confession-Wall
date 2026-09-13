import React, { useState } from 'react';
import { Heart, Clock, User, Share2 } from 'lucide-react';
import { likeConfession } from '../services/api';

/**
 * Format timestamp into human readable relative or absolute Vietnamese format
 * @param {string|Date} dateString
 * @returns {string} Formatted string
 */
function formatVietnameseTime(dateString) {
  if (!dateString) return 'vừa xong';

  const date = new Date(dateString);
  if (isNaN(date.getTime())) return 'vừa xong';

  const now = new Date();
  const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

  if (diffInSeconds < 30) {
    return 'vừa xong';
  } else if (diffInSeconds < 60) {
    return `${diffInSeconds} giây trước`;
  }

  const diffInMinutes = Math.floor(diffInSeconds / 60);
  if (diffInMinutes < 60) {
    return `${diffInMinutes} phút trước`;
  }

  const diffInHours = Math.floor(diffInMinutes / 60);
  if (diffInHours < 24) {
    return `${diffInHours} giờ trước`;
  }

  const diffInDays = Math.floor(diffInHours / 24);
  if (diffInDays < 7) {
    return `${diffInDays} ngày trước`;
  }

  // Fallback to formatted date DD/MM/YYYY HH:mm
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = date.getFullYear();
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');

  return `${day}/${month}/${year} ${hours}:${minutes}`;
}

export default function ConfessionCard({ confession, onLikeUpdate }) {
  const [likes, setLikes] = useState(confession.likes || 0);
  const [isLiking, setIsLiking] = useState(false);
  const [hasLiked, setHasLiked] = useState(false);
  const [animateHeart, setAnimateHeart] = useState(false);

  const handleLike = async () => {
    if (isLiking) return;

    // Optimistic UI update
    const previousLikes = likes;
    const nextLikes = previousLikes + 1;

    setLikes(nextLikes);
    setHasLiked(true);
    setAnimateHeart(true);
    setIsLiking(true);

    setTimeout(() => {
      setAnimateHeart(false);
    }, 800);

    try {
      const updatedEntity = await likeConfession(confession.id);
      if (updatedEntity && typeof updatedEntity.likes === 'number') {
        setLikes(updatedEntity.likes);
        if (onLikeUpdate) {
          onLikeUpdate(confession.id, updatedEntity.likes);
        }
      }
    } catch (err) {
      console.error(`Lỗi khi thả tim cho confession #${confession.id}:`, err);
      // Rollback on failure
      setLikes(previousLikes);
      setHasLiked(false);
    } finally {
      setIsLiking(false);
    }
  };

  const authorName = confession.author?.trim() || 'Ẩn danh';
  const isAnonymous = authorName === 'Ẩn danh';

  return (
    <article className="group bg-white rounded-2xl p-5 border border-slate-100 shadow-card hover:shadow-card-hover transition-all duration-300 flex flex-col justify-between hover:-translate-y-1 relative overflow-hidden">
      {/* Decorative top gradient accent bar */}
      <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-rose-400 via-pink-400 to-amber-300 opacity-60 group-hover:opacity-100 transition-opacity" />

      <div>
        {/* Author & Timestamp Header */}
        <div className="flex items-center justify-between gap-2 mb-3 pb-3 border-b border-slate-50">
          <div className="flex items-center gap-2 min-w-0">
            <div
              className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 text-xs font-semibold ${
                isAnonymous
                  ? 'bg-slate-100 text-slate-500'
                  : 'bg-rose-100 text-rose-600'
              }`}
            >
              <User className="w-4 h-4" />
            </div>
            <div className="min-w-0">
              <span className="block font-medium text-slate-800 text-sm truncate">
                {authorName}
              </span>
              <span className="flex items-center gap-1 text-[11px] text-slate-400">
                <Clock className="w-3 h-3" />
                <time dateTime={confession.createdAt}>
                  {formatVietnameseTime(confession.createdAt)}
                </time>
              </span>
            </div>
          </div>

          <span className="text-[11px] font-mono text-slate-300 group-hover:text-slate-400 transition-colors">
            #{confession.id}
          </span>
        </div>

        {/* Message Content */}
        <div className="text-slate-700 text-sm leading-relaxed whitespace-pre-line break-words my-2 font-normal">
          {confession.content}
        </div>
      </div>

      {/* Card Actions Footer */}
      <div className="pt-3 mt-3 border-t border-slate-100/80 flex items-center justify-between">
        <button
          type="button"
          onClick={handleLike}
          disabled={isLiking}
          aria-label={`Thả tim cho lời thú tội #${confession.id}, hiện có ${likes} tim`}
          className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium transition-all ${
            hasLiked
              ? 'bg-rose-50 text-rose-600 border border-rose-200 shadow-sm'
              : 'bg-slate-50 text-slate-600 hover:bg-rose-50 hover:text-rose-600 border border-slate-200 hover:border-rose-200'
          }`}
        >
          <Heart
            className={`w-3.5 h-3.5 transition-transform ${
              hasLiked ? 'fill-rose-500 text-rose-500' : 'text-slate-400 group-hover:text-rose-500'
            } ${animateHeart ? 'scale-125 text-rose-600 fill-rose-600 animate-pulse' : ''}`}
          />
          <span>❤️ Thả tim</span>
          <span
            className={`ml-0.5 px-1.5 py-0.2 rounded-full text-[11px] font-bold ${
              hasLiked ? 'bg-rose-100 text-rose-700' : 'bg-slate-200/60 text-slate-600'
            }`}
          >
            {likes}
          </span>
        </button>

        <span className="text-[11px] text-slate-400 italic">
          Lời thú tội
        </span>
      </div>
    </article>
  );
}
