import React, { useState, useRef } from 'react';
import { Heart, Clock, User } from 'lucide-react';
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
  const [tiltStyle, setTiltStyle] = useState({});
  const cardRef = useRef(null);

  const handleMouseMove = (e) => {
    if (!cardRef.current) return;
    const rect = cardRef.current.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    const centerX = rect.width / 2;
    const centerY = rect.height / 2;
    const rotateX = ((y - centerY) / centerY) * -9;
    const rotateY = ((x - centerX) / centerX) * 9;

    setTiltStyle({
      transform: `perspective(1000px) rotateX(${rotateX.toFixed(2)}deg) rotateY(${rotateY.toFixed(2)}deg) translateZ(6px)`,
      transition: 'transform 0.1s ease-out',
    });
  };

  const handleMouseLeave = () => {
    setTiltStyle({
      transform: 'perspective(1000px) rotateX(0deg) rotateY(0deg) translateZ(0px)',
      transition: 'transform 0.35s ease-out',
    });
  };

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
    <article
      ref={cardRef}
      onMouseMove={handleMouseMove}
      onMouseLeave={handleMouseLeave}
      style={tiltStyle}
      className="group bg-slate-900/75 backdrop-blur-md rounded-2xl p-5 border border-slate-800/80 hover:border-purple-800/60 shadow-card hover:shadow-[0_0_25px_rgba(168,85,247,0.18)] transition-all duration-300 flex flex-col justify-between relative overflow-hidden will-change-transform"
    >
      {/* Decorative top amethyst gradient accent bar */}
      <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-purple-950 via-purple-500 to-fuchsia-950 opacity-70 group-hover:opacity-100 group-hover:shadow-[0_0_14px_rgba(168,85,247,0.7)] transition-all" />

      {/* Subtle hover dynamic sheen */}
      <div className="absolute inset-0 bg-gradient-to-tr from-purple-500/0 via-purple-500/5 to-purple-400/10 opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none" />

      <div className="relative z-10">
        {/* Author & Timestamp Header */}
        <div className="flex items-center justify-between gap-2 mb-3 pb-3 border-b border-slate-800/70">
          <div className="flex items-center gap-2 min-w-0">
            <div
              className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 text-xs font-semibold ${
                isAnonymous
                  ? 'bg-slate-800/90 text-slate-400 border border-slate-700/60'
                  : 'bg-purple-950/70 text-purple-300 border border-purple-800/60 shadow-[0_0_8px_rgba(168,85,247,0.25)]'
              }`}
            >
              <User className="w-4 h-4" />
            </div>
            <div className="min-w-0">
              <span className="block font-medium text-slate-200 text-sm truncate group-hover:text-purple-200 transition-colors">
                {authorName}
              </span>
              <span className="flex items-center gap-1 text-[11px] text-slate-400">
                <Clock className="w-3 h-3 text-slate-500" />
                <time dateTime={confession.createdAt}>
                  {formatVietnameseTime(confession.createdAt)}
                </time>
              </span>
            </div>
          </div>

          <span className="text-[11px] font-mono text-slate-500 group-hover:text-purple-400/70 transition-colors">
            #{confession.id}
          </span>
        </div>

        {/* Message Content */}
        <div className="text-slate-300 text-sm leading-relaxed whitespace-pre-line break-words my-2 font-normal selection:bg-purple-950 selection:text-purple-300">
          {confession.content}
        </div>
      </div>

      {/* Card Actions Footer */}
      <div className="pt-3 mt-3 border-t border-slate-800/70 flex items-center justify-between relative z-10">
        <button
          type="button"
          onClick={handleLike}
          disabled={isLiking}
          aria-label={`Thả tim cho lời thú tội #${confession.id}, hiện có ${likes} tim`}
          className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium transition-all duration-200 ${
            hasLiked
              ? 'bg-purple-950/70 text-purple-200 border border-purple-700/70 shadow-[0_0_12px_rgba(168,85,247,0.35)]'
              : 'bg-slate-800/80 text-slate-300 hover:bg-purple-950/50 hover:text-purple-200 border border-slate-700/60 hover:border-purple-800/60'
          }`}
        >
          <Heart
            className={`w-3.5 h-3.5 transition-transform ${
              hasLiked
                ? 'fill-purple-400 text-purple-400 drop-shadow-[0_0_6px_rgba(192,132,252,0.9)]'
                : 'text-slate-400 group-hover:text-purple-400'
            } ${animateHeart ? 'scale-125 text-purple-400 fill-purple-400 animate-pulse' : ''}`}
          />
          <span>💜 Thả tim</span>
          <span
            className={`ml-0.5 px-1.5 py-0.2 rounded-full text-[11px] font-bold ${
              hasLiked ? 'bg-purple-900/80 text-purple-200' : 'bg-slate-700/60 text-slate-300'
            }`}
          >
            {likes}
          </span>
        </button>

        <span className="text-[11px] text-slate-500 italic">
          Lời thú tội
        </span>
      </div>
    </article>
  );
}
