import React, { useState, useEffect } from 'react';
import { Send, User, MessageSquare, AlertCircle, CheckCircle2, Loader2, Clock } from 'lucide-react';
import { createConfession } from '../services/api';

export default function ConfessionForm({ onConfessionCreated }) {
  const [content, setContent] = useState('');
  const [author, setAuthor] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [cooldown, setCooldown] = useState(0);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  useEffect(() => {
    if (cooldown <= 0) return;
    const timer = setInterval(() => {
      setCooldown((prev) => (prev > 1 ? prev - 1 : 0));
    }, 1000);
    return () => clearInterval(timer);
  }, [cooldown]);

  const handleSubmit = async (e) => {
    if (e) e.preventDefault();

    if (cooldown > 0) {
      setErrorMessage(`Bạn đang thao tác quá nhanh. Vui lòng chờ ${cooldown} giây trước khi gửi tiếp.`);
      return;
    }

    // Client-side validation: reject empty or whitespace-only content
    const trimmedContent = content.trim();
    if (!trimmedContent) {
      setErrorMessage('Nội dung lời thú tội không được để trống.');
      setSuccessMessage('');
      return;
    }

    // Clear previous error
    setErrorMessage('');
    setIsSubmitting(true);

    try {
      const payload = {
        content: trimmedContent,
        author: author.trim() || 'Ẩn danh',
      };

      const newConfession = await createConfession(payload);

      // Reset form fields
      setContent('');
      setAuthor('');
      setSuccessMessage('Lời thú tội của bạn đã được gửi thành công!');

      // Set gentle cooldown to prevent double submit
      setCooldown(5);

      // Notify parent component to update confession list
      if (onConfessionCreated) {
        onConfessionCreated(newConfession);
      }

      // Auto clear success message after 4 seconds
      setTimeout(() => {
        setSuccessMessage('');
      }, 4000);
    } catch (err) {
      console.error('Lỗi khi gửi lời thú tội:', err);
      const serverMessage =
        err.friendlyMessage ||
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Không thể gửi lời thú tội. Vui lòng thử lại sau.';
      if (err.response?.status === 429) {
        setCooldown(15);
      }
      setErrorMessage(serverMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleKeyDown = (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
      handleSubmit(e);
    }
  };

  return (
    <div className="bg-slate-900/75 backdrop-blur-md rounded-2xl shadow-card border border-slate-800/80 hover:border-rose-950/60 p-6 transition-all duration-300 hover:shadow-card-hover">
      <div className="flex items-center gap-2 mb-4">
        <div className="p-2 rounded-xl bg-rose-950/60 text-rose-400 border border-rose-900/50 shadow-[0_0_10px_rgba(225,29,72,0.25)]">
          <MessageSquare className="w-5 h-5" />
        </div>
        <div>
          <h2 className="text-lg font-bold text-slate-100">
            Gửi lời thú tội mới
          </h2>
          <p className="text-xs text-slate-400">
            Chia sẻ câu chuyện của bạn trong bóng tối, tất cả đều được bảo mật
          </p>
        </div>
      </div>

      {/* Error Alert Banner */}
      {errorMessage && (
        <div
          role="alert"
          className="mb-4 p-3 rounded-xl bg-red-950/50 border border-red-850/60 text-red-300 text-sm flex items-start gap-2.5 animate-fadeIn"
        >
          <AlertCircle className="w-5 h-5 text-rose-500 flex-shrink-0 mt-0.5" />
          <span className="font-medium">{errorMessage}</span>
        </div>
      )}

      {/* Success Alert Banner */}
      {successMessage && (
        <div
          role="status"
          className="mb-4 p-3 rounded-xl bg-emerald-950/50 border border-emerald-800/60 text-emerald-300 text-sm flex items-start gap-2.5 animate-fadeIn"
        >
          <CheckCircle2 className="w-5 h-5 text-emerald-400 flex-shrink-0 mt-0.5" />
          <span className="font-medium">{successMessage}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Content Textarea */}
        <div>
          <label
            htmlFor="confession-content"
            className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider"
          >
            Nội dung thú tội <span className="text-rose-500">*</span>
          </label>
          <textarea
            id="confession-content"
            rows="4"
            required
            value={content}
            onChange={(e) => {
              setContent(e.target.value);
              if (errorMessage) setErrorMessage('');
            }}
            onKeyDown={handleKeyDown}
            placeholder="Bạn đang nghĩ gì? Hãy chia sẻ thật lòng tại đây... (Nhấn Ctrl+Enter để gửi nhanh)"
            disabled={isSubmitting || cooldown > 0}
            className="w-full px-4 py-3 rounded-xl border border-slate-800 bg-slate-950/70 text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-rose-500/40 focus:border-rose-600 transition-all resize-none text-sm leading-relaxed disabled:bg-slate-950/40 disabled:cursor-not-allowed"
          />
          <div className="flex justify-between items-center mt-1 text-xs text-slate-500">
            <span>Không chia sẻ thông tin xúc phạm hoặc nhạy cảm</span>
            <span>{content.length} ký tự</span>
          </div>
        </div>

        {/* Author Input & Submit */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 items-end">
          <div>
            <label
              htmlFor="confession-author"
              className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider"
            >
              Tên của bạn (Tùy chọn)
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                <User className="w-4 h-4" />
              </div>
              <input
                id="confession-author"
                type="text"
                value={author}
                onChange={(e) => setAuthor(e.target.value)}
                placeholder="Ẩn danh (tuỳ chọn)"
                disabled={isSubmitting || cooldown > 0}
                maxLength={50}
                className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-800 bg-slate-950/70 text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-rose-500/40 focus:border-rose-600 transition-all text-sm disabled:bg-slate-950/40 disabled:cursor-not-allowed"
              />
            </div>
          </div>

          <div>
            <button
              type="submit"
              disabled={isSubmitting || cooldown > 0}
              className="w-full py-2.5 px-6 rounded-xl bg-gradient-to-r from-purple-700 via-purple-600 to-indigo-700 hover:from-purple-600 hover:to-purple-700 active:scale-[0.98] text-purple-50 font-medium text-sm shadow-lg shadow-purple-950/50 hover:shadow-[0_0_20px_rgba(168,85,247,0.4)] border border-purple-500/30 transition-all flex items-center justify-center gap-2 disabled:opacity-60 disabled:cursor-not-allowed disabled:transform-none"
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Đang gửi...</span>
                </>
              ) : cooldown > 0 ? (
                <>
                  <Clock className="w-4 h-4 animate-spin" />
                  <span>Vui lòng chờ {cooldown}s...</span>
                </>
              ) : (
                <>
                  <Send className="w-4 h-4" />
                  <span>Gửi lời thú tội</span>
                </>
              )}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}
