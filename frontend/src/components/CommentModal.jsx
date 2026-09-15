import React, { useState, useEffect, useRef } from 'react';
import { X, Send, User, Clock, MessageSquare, AlertTriangle, Loader2 } from 'lucide-react';
import { getComments, createComment } from '../services/api';

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

export default function CommentModal({
  confession,
  isOpen,
  onClose,
  onCommentAdded,
  realtimeComment,
}) {
  const [comments, setComments] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [author, setAuthor] = useState('');
  const [content, setContent] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const commentsEndRef = useRef(null);
  const textareaRef = useRef(null);
  const isInitialLoadRef = useRef(true);

  // Lock body scroll, listen for ESC key, and autofocus textarea
  useEffect(() => {
    if (!isOpen) return;

    isInitialLoadRef.current = true;
    const focusTimer = setTimeout(() => {
      textareaRef.current?.focus();
    }, 100);

    const handleKeyDown = (e) => {
      if (e.key === 'Escape') {
        onClose();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    const originalOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';

    return () => {
      clearTimeout(focusTimer);
      window.removeEventListener('keydown', handleKeyDown);
      document.body.style.overflow = originalOverflow;
    };
  }, [isOpen, onClose]);

  // Fetch comments when modal opens for a confession
  useEffect(() => {
    if (!isOpen || !confession?.id) return;

    let isSubscribed = true;
    setIsLoading(true);
    setErrorMessage('');
    isInitialLoadRef.current = true;

    getComments(confession.id)
      .then((data) => {
        if (isSubscribed) {
          setComments(Array.isArray(data) ? data : []);
        }
      })
      .catch((err) => {
        if (isSubscribed) {
          setErrorMessage(
            err.response?.data?.message || 'Không thể tải danh sách bình luận. Vui lòng thử lại.'
          );
        }
      })
      .finally(() => {
        if (isSubscribed) {
          setIsLoading(false);
        }
      });

    return () => {
      isSubscribed = false;
    };
  }, [isOpen, confession?.id]);

  // Sync real-time comment from SSE stream without reload
  useEffect(() => {
    if (!realtimeComment || !isOpen || !confession?.id) return;

    const commentData = realtimeComment.comment || (realtimeComment.id ? realtimeComment : null);
    const targetConfessionId = realtimeComment.confessionId || commentData?.confessionId;

    if (commentData && targetConfessionId === confession.id) {
      setComments((prev) => {
        if (prev.some((c) => c.id === commentData.id)) {
          return prev;
        }
        return [...prev, commentData];
      });
    }
  }, [realtimeComment, isOpen, confession?.id]);

  // Scroll to bottom when new comment arrives (skip initial load to keep confession text in view)
  useEffect(() => {
    if (isLoading) return;
    if (isInitialLoadRef.current) {
      isInitialLoadRef.current = false;
      return;
    }
    if (comments.length > 0) {
      commentsEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [comments.length, isLoading]);

  if (!isOpen || !confession) return null;

  const handleSubmit = async (e) => {
    if (e) e.preventDefault();
    if (isSubmitting) return;

    const trimmedContent = content.trim();

    if (!trimmedContent) {
      setErrorMessage('Vui lòng nhập nội dung bình luận.');
      return;
    }

    if (trimmedContent.length > 500) {
      setErrorMessage('Nội dung bình luận không được vượt quá 500 ký tự.');
      return;
    }

    setIsSubmitting(true);
    setErrorMessage('');

    try {
      const newComment = await createComment(confession.id, {
        content: trimmedContent,
        author: author.trim() || undefined,
      });

      // Optimistically append new comment if not already added by SSE
      setComments((prev) => {
        if (prev.some((c) => c.id === newComment.id)) return prev;
        return [...prev, newComment];
      });

      // Reset form
      setContent('');
      setAuthor('');
      onCommentAdded?.(confession.id, newComment);
    } catch (err) {
      console.error('Lỗi khi gửi bình luận:', err);
      setErrorMessage(
        err.response?.data?.message ||
        (err.response?.status === 429
          ? 'Bạn đang thao tác quá nhanh, vui lòng thử lại sau.'
          : 'Không thể gửi bình luận. Vui lòng thử lại.')
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleTextareaKeyDown = (e) => {
    // Ctrl + Enter or Cmd + Enter to submit
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
      e.preventDefault();
      handleSubmit(e);
    }
  };

  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  const authorName = confession.author?.trim() || 'Ẩn danh';

  return (
    <div
      onClick={handleBackdropClick}
      onKeyDown={(e) => {
        if (e.key === 'Escape') {
          onClose();
        }
      }}
      tabIndex={-1}
      className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-3 sm:p-6 transition-all duration-300"
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-title"
    >
      <div className="bg-slate-900 border border-slate-800/90 rounded-2xl max-w-2xl w-full max-h-[92vh] flex flex-col shadow-[0_20px_60px_rgba(0,0,0,0.85),0_0_30px_rgba(168,85,247,0.2)] overflow-hidden animate-confession-appear">
        {/* Top amethyst accent bar */}
        <div className="h-1 bg-gradient-to-r from-purple-950 via-purple-400 to-fuchsia-950 shadow-[0_0_12px_rgba(168,85,247,0.6)]" />

        {/* Modal Header */}
        <div className="p-4 sm:p-5 border-b border-slate-800 flex items-center justify-between gap-3 bg-slate-900/90">
          <div className="flex items-center gap-2.5 min-w-0">
            <div className="w-8 h-8 rounded-full bg-purple-950/80 border border-purple-700/80 text-purple-300 flex items-center justify-center flex-shrink-0 text-xs font-semibold shadow-[0_0_10px_rgba(168,85,247,0.3)]">
              <MessageSquare className="w-4 h-4" />
            </div>
            <div className="min-w-0">
              <h3 id="modal-title" className="font-bold text-slate-100 text-base sm:text-lg truncate">
                Bình luận thú tội #{confession.id}
              </h3>
              <p className="text-xs text-slate-400 flex items-center gap-1.5">
                <span>{authorName}</span>
                <span>&bull;</span>
                <span className="flex items-center gap-1">
                  <Clock className="w-3 h-3 text-slate-500" />
                  {formatVietnameseTime(confession.createdAt)}
                </span>
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={onClose}
            aria-label="Đóng cửa sổ bình luận"
            className="w-8 h-8 rounded-full bg-slate-800/80 hover:bg-slate-700 border border-slate-700 text-slate-400 hover:text-slate-100 flex items-center justify-center transition-colors flex-shrink-0"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Modal Body */}
        <div className="flex-1 overflow-y-auto p-4 sm:p-5 space-y-4">
          {/* Original Confession Card Preview */}
          <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800/90 shadow-inner">
            <p className="text-slate-200 text-sm leading-relaxed whitespace-pre-line break-words font-medium">
              {confession.content}
            </p>
          </div>

          {/* Comments List Header */}
          <div className="flex items-center justify-between pt-1">
            <span className="text-xs font-semibold text-slate-300 flex items-center gap-1.5">
              <span>Các bình luận</span>
              <span className="px-2 py-0.5 rounded-full text-[11px] bg-purple-950 text-purple-300 border border-purple-800/60 font-bold">
                {comments.length}
              </span>
            </span>
          </div>

          {/* Comments List */}
          <div className="space-y-3 min-h-[140px]">
            {isLoading ? (
              <div className="flex flex-col items-center justify-center py-10 text-slate-500 space-y-2">
                <Loader2 className="w-6 h-6 animate-spin text-purple-400" />
                <span className="text-xs">Đang tải bình luận...</span>
              </div>
            ) : comments.length === 0 ? (
              <div className="text-center py-8 px-4 rounded-xl bg-slate-950/30 border border-dashed border-slate-800 text-slate-400">
                <p className="text-sm font-medium text-slate-300">Chưa có bình luận nào</p>
                <p className="text-xs text-slate-500 mt-1">
                  Hãy là người đầu tiên chia sẻ cảm nghĩ về lời thú tội này!
                </p>
              </div>
            ) : (
              comments.map((comment) => {
                const commentAuthor = comment.author?.trim() || 'Ẩn danh';
                const isAnon = commentAuthor === 'Ẩn danh';
                return (
                  <div
                    key={comment.id || `${comment.createdAt}-${comment.content}`}
                    className="p-3.5 rounded-xl bg-slate-950/60 border border-slate-800/80 hover:border-purple-800/40 transition-colors animate-confession-appear"
                  >
                    <div className="flex items-center justify-between gap-2 mb-1.5">
                      <div className="flex items-center gap-2 min-w-0">
                        <div
                          className={`w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold ${
                            isAnon
                              ? 'bg-slate-800 text-slate-400 border border-slate-700'
                              : 'bg-purple-950 text-purple-300 border border-purple-700'
                          }`}
                        >
                          <User className="w-3 h-3" />
                        </div>
                        <span className="text-xs font-semibold text-slate-200 truncate">
                          {commentAuthor}
                        </span>
                      </div>
                      <span className="text-[11px] text-slate-500 flex-shrink-0">
                        {formatVietnameseTime(comment.createdAt)}
                      </span>
                    </div>
                    <p className="text-sm text-slate-300 leading-relaxed whitespace-pre-line break-words pl-7">
                      {comment.content}
                    </p>
                  </div>
                );
              })
            )}
            <div ref={commentsEndRef} />
          </div>
        </div>

        {/* Modal Footer / New Comment Form */}
        <form onSubmit={handleSubmit} className="p-4 sm:p-5 border-t border-slate-800 bg-slate-900/95 space-y-3">
          {errorMessage && (
            <div className="p-2.5 rounded-xl bg-amber-950/80 border border-amber-600/60 text-amber-200 text-xs flex items-center gap-2 animate-fadeIn">
              <AlertTriangle className="w-4 h-4 text-amber-400 flex-shrink-0" />
              <span>{errorMessage}</span>
            </div>
          )}

          <div className="flex flex-col sm:flex-row gap-2">
            <div className="w-full sm:w-1/3">
              <label htmlFor="comment-author" className="sr-only">Tên người bình luận (tùy chọn)</label>
              <input
                id="comment-author"
                type="text"
                maxLength={50}
                value={author}
                onChange={(e) => setAuthor(e.target.value)}
                placeholder="Tên bạn (mặc định Ẩn danh)"
                className="w-full px-3.5 py-2 rounded-xl bg-slate-950 border border-slate-800 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-purple-500 focus:ring-1 focus:ring-purple-500 transition-all"
              />
            </div>
            <div className="flex-1 text-right text-[11px] text-slate-500 self-center hidden sm:block">
              {content.length}/500 ký tự &bull; <kbd className="px-1.5 py-0.5 rounded bg-slate-800 text-[10px] text-slate-400 font-mono">Ctrl/Cmd + Enter</kbd> để gửi
            </div>
          </div>

          <div className="relative">
            <label htmlFor="comment-content" className="sr-only">Nội dung bình luận</label>
            <textarea
              id="comment-content"
              ref={textareaRef}
              rows={2}
              maxLength={500}
              required
              value={content}
              onChange={(e) => {
                setContent(e.target.value);
                if (errorMessage) setErrorMessage('');
              }}
              onKeyDown={handleTextareaKeyDown}
              placeholder="Chia sẻ suy nghĩ của bạn về lời thú tội này..."
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-sm text-slate-200 placeholder-slate-500 focus:outline-none focus:border-purple-500 focus:ring-1 focus:ring-purple-500 transition-all resize-none pr-24"
            />
            <button
              type="submit"
              disabled={isSubmitting || !content.trim()}
              className="absolute right-2 bottom-3 px-3 py-1.5 rounded-lg bg-purple-600 hover:bg-purple-500 disabled:bg-slate-800 disabled:text-slate-500 text-white text-xs font-semibold flex items-center gap-1.5 transition-all shadow-md active:scale-95"
            >
              {isSubmitting ? (
                <Loader2 className="w-3.5 h-3.5 animate-spin" />
              ) : (
                <Send className="w-3.5 h-3.5" />
              )}
              <span>Gửi</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
