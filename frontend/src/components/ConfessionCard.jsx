import React, { useState, useRef, useEffect } from 'react';
import { Heart, Clock, User, AlertTriangle } from 'lucide-react';
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
  const [warningMessage, setWarningMessage] = useState('');
  const [tiltStyle, setTiltStyle] = useState({
    transform: 'perspective(1200px) rotateX(0deg) rotateY(0deg) translateZ(0px) scale3d(1, 1, 1)',
    boxShadow: '0 10px 30px -10px rgba(0, 0, 0, 0.6), 0 0 1px 1px rgba(168, 85, 247, 0.15)',
  });
  const [glareStyle, setGlareStyle] = useState({
    opacity: 0,
  });
  const cardRef = useRef(null);
  const lastLikeTimestamp = useRef(0);

  useEffect(() => {
    if (typeof confession.likes === 'number') {
      setLikes(confession.likes);
    }
  }, [confession.likes]);

  const handleMouseMove = (e) => {
    if (!cardRef.current) return;
    const rect = cardRef.current.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    const centerX = rect.width / 2;
    const centerY = rect.height / 2;

    // Stronger, noticeable 3D tilt: up to 16 degrees
    const rotateX = ((y - centerY) / centerY) * -16;
    const rotateY = ((x - centerX) / centerX) * 16;

    // Dynamic light glare following the cursor
    const glareX = (x / rect.width) * 100;
    const glareY = (y / rect.height) * 100;

    // Dynamic 3D depth shadow that responds in real-time to the tilt
    const shadowX = (rotateY * -1.5).toFixed(1);
    const shadowY = (Math.abs(rotateX) + 14).toFixed(1);

    setTiltStyle({
      transform: `perspective(1200px) rotateX(${rotateX.toFixed(2)}deg) rotateY(${rotateY.toFixed(2)}deg) translateZ(24px) scale3d(1.03, 1.03, 1.03)`,
      boxShadow: `${shadowX}px ${shadowY}px 40px rgba(0, 0, 0, 0.8), 0 0 30px rgba(168, 85, 247, 0.35)`,
      transition: 'transform 0.08s ease-out, box-shadow 0.08s ease-out',
    });

    setGlareStyle({
      background: `radial-gradient(circle at ${glareX.toFixed(1)}% ${glareY.toFixed(1)}%, rgba(192, 132, 252, 0.25) 0%, rgba(168, 85, 247, 0.08) 40%, transparent 70%)`,
      opacity: 1,
      transition: 'opacity 0.15s ease-out',
    });
  };

  const handleMouseLeave = () => {
    setTiltStyle({
      transform: 'perspective(1200px) rotateX(0deg) rotateY(0deg) translateZ(0px) scale3d(1, 1, 1)',
      boxShadow: '0 10px 30px -10px rgba(0, 0, 0, 0.6), 0 0 1px 1px rgba(168, 85, 247, 0.15)',
      transition: 'transform 0.5s cubic-bezier(0.23, 1, 0.32, 1), box-shadow 0.5s ease-out',
    });
    setGlareStyle({
      opacity: 0,
      transition: 'opacity 0.4s ease-out',
    });
  };

  const handleLike = async () => {
    const now = Date.now();
    if (now - lastLikeTimestamp.current < 1500) {
      setWarningMessage('Thả tim hơi nhanh rồi, chậm lại xíu nhé!');
      setTimeout(() => setWarningMessage(''), 2500);
      return;
    }
    if (isLiking) return;
    lastLikeTimestamp.current = now;

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
      const msg =
        err.friendlyMessage ||
        (err.response?.status === 429
          ? 'Bạn đang thao tác quá nhanh, vui lòng thử lại sau.'
          : 'Không thể thả tim. Vui lòng thử lại sau.');
      setWarningMessage(msg);
      setTimeout(() => setWarningMessage(''), 3000);
    } finally {
      setIsLiking(false);
    }
  };

  const authorName = confession.author?.trim() || 'Ẩn danh';
  const isAnonymous = authorName === 'Ẩn danh';

  return (
    <div className="[perspective:1200px] h-full">
      <article
        ref={cardRef}
        onMouseMove={handleMouseMove}
        onMouseLeave={handleMouseLeave}
        style={tiltStyle}
        className="group bg-slate-900/85 backdrop-blur-md rounded-2xl p-5 border border-slate-800/90 hover:border-purple-500/70 transition-colors duration-200 flex flex-col justify-between relative will-change-transform [transform-style:preserve-3d] cursor-pointer animate-confession-appear h-full select-none"
      >
        {/* Background rounded container for border glow and dynamic specular glare */}
        <div className="absolute inset-0 rounded-2xl overflow-hidden pointer-events-none">
          {/* Decorative top amethyst gradient accent bar */}
          <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-purple-950 via-purple-400 to-fuchsia-950 opacity-70 group-hover:opacity-100 group-hover:shadow-[0_0_16px_rgba(168,85,247,0.9)] transition-all" />

          {/* Dynamic 3D mouse glare reflection */}
          <div
            style={glareStyle}
            className="absolute inset-0 pointer-events-none"
          />
        </div>

        {/* Anti-spam warning banner */}
        {warningMessage && (
          <div
            style={{ transform: 'translateZ(30px)' }}
            className="mb-2 p-2 rounded-xl bg-amber-950/80 border border-amber-600/60 text-amber-200 text-xs flex items-center gap-1.5 animate-fadeIn z-30"
          >
            <AlertTriangle className="w-3.5 h-3.5 text-amber-400 flex-shrink-0" />
            <span className="font-medium">{warningMessage}</span>
          </div>
        )}

        {/* 3D Floating Layer 1: Header (Author & Timestamp) */}
        <div
          style={{ transform: 'translateZ(28px)', transformStyle: 'preserve-3d' }}
          className="relative z-10 transition-transform duration-100"
        >
          <div className="flex items-center justify-between gap-2 mb-3 pb-3 border-b border-slate-800/80">
            <div className="flex items-center gap-2.5 min-w-0">
              <div
                style={{ transform: 'translateZ(10px)' }}
                className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 text-xs font-semibold shadow-md ${
                  isAnonymous
                    ? 'bg-slate-800 text-slate-400 border border-slate-700/80'
                    : 'bg-purple-950 text-purple-300 border border-purple-700/80 shadow-[0_0_12px_rgba(168,85,247,0.35)]'
                }`}
              >
                <User className="w-4 h-4" />
              </div>
              <div className="min-w-0">
                <span className="block font-semibold text-slate-200 text-sm truncate group-hover:text-purple-200 transition-colors">
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

            <span
              style={{ transform: 'translateZ(12px)' }}
              className="text-[11px] font-mono px-2 py-0.5 rounded-md bg-slate-850 border border-slate-800 text-slate-400 group-hover:text-purple-300 group-hover:border-purple-800/60 transition-colors shadow-sm"
            >
              #{confession.id}
            </span>
          </div>
        </div>

        {/* 3D Floating Layer 2: Confession Message Content */}
        <div
          style={{ transform: 'translateZ(18px)' }}
          className="text-slate-300 text-sm leading-relaxed whitespace-pre-line break-words my-3 font-normal selection:bg-purple-950 selection:text-purple-300 flex-1 relative z-10 transition-transform duration-100"
        >
          {confession.content}
        </div>

        {/* 3D Floating Layer 3: Card Actions & Like Button (Highest elevation) */}
        <div
          style={{ transform: 'translateZ(36px)', transformStyle: 'preserve-3d' }}
          className="pt-3 mt-3 border-t border-slate-800/80 flex items-center justify-between relative z-20 transition-transform duration-100"
        >
          <button
            type="button"
            onClick={handleLike}
            disabled={isLiking}
            aria-label={`Thả tim cho lời thú tội #${confession.id}, hiện có ${likes} tim`}
            style={{ transform: 'translateZ(8px)' }}
            className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium transition-all duration-200 active:scale-95 shadow-md ${
              hasLiked
                ? 'bg-purple-900/90 text-purple-100 border border-purple-500/80 shadow-[0_0_16px_rgba(168,85,247,0.5)]'
                : 'bg-slate-800/90 text-slate-300 hover:bg-purple-950/70 hover:text-purple-100 border border-slate-700/80 hover:border-purple-600/70 hover:shadow-[0_0_12px_rgba(168,85,247,0.25)]'
            }`}
          >
            <Heart
              className={`w-3.5 h-3.5 transition-transform ${
                hasLiked
                  ? 'fill-purple-400 text-purple-400 drop-shadow-[0_0_8px_rgba(192,132,252,0.95)]'
                  : 'text-slate-400 group-hover:text-purple-400'
              } ${animateHeart ? 'scale-125 text-purple-400 fill-purple-400 animate-pulse' : ''}`}
            />
            <span>💜 Thả tim</span>
            <span
              className={`ml-0.5 px-1.5 py-0.2 rounded-full text-[11px] font-bold ${
                hasLiked ? 'bg-purple-950 text-purple-200' : 'bg-slate-700 text-slate-200'
              }`}
            >
              {likes}
            </span>
          </button>

          <span
            style={{ transform: 'translateZ(5px)' }}
            className="text-[11px] text-slate-500 italic"
          >
            Lời thú tội
          </span>
        </div>
      </article>
    </div>
  );
}
