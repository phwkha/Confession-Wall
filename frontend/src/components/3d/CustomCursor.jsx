import React, { useState, useEffect, useRef } from 'react';

export default function CustomCursor() {
  const [position, setPosition] = useState({ x: -100, y: -100 });
  const [trailingPos, setTrailingPos] = useState({ x: -100, y: -100 });
  const [isHovered, setIsHovered] = useState(false);
  const [isClicked, setIsClicked] = useState(false);
  const [isVisible, setIsVisible] = useState(false);
  const [isTouchDevice, setIsTouchDevice] = useState(false);

  const requestRef = useRef(null);
  const targetPosRef = useRef({ x: -100, y: -100 });
  const currentPosRef = useRef({ x: -100, y: -100 });

  useEffect(() => {
    // Detect mobile or touch screen devices
    if (typeof window !== 'undefined') {
      const isTouch = window.matchMedia('(pointer: coarse)').matches || 'ontouchstart' in window;
      setIsTouchDevice(isTouch);
      if (isTouch) return;
    }

    const handleMouseMove = (e) => {
      targetPosRef.current = { x: e.clientX, y: e.clientY };
      setPosition({ x: e.clientX, y: e.clientY });
      if (!isVisible) setIsVisible(true);

      // Check if hovering over clickable or interactive element
      const target = e.target;
      const isInteractive = Boolean(
        target.closest('button, a, input, textarea, select, [role="button"], article, .cursor-pointer')
      );
      setIsHovered(isInteractive);
    };

    const handleMouseDown = () => setIsClicked(true);
    const handleMouseUp = () => setIsClicked(false);
    const handleMouseLeave = () => setIsVisible(false);
    const handleMouseEnter = () => setIsVisible(true);

    window.addEventListener('mousemove', handleMouseMove, { passive: true });
    window.addEventListener('mousedown', handleMouseDown);
    window.addEventListener('mouseup', handleMouseUp);
    document.addEventListener('mouseleave', handleMouseLeave);
    document.addEventListener('mouseenter', handleMouseEnter);

    // Smooth linear interpolation (lerp) loop for trailing luminous aura
    const animate = () => {
      const lerpFactor = 0.22;
      currentPosRef.current.x += (targetPosRef.current.x - currentPosRef.current.x) * lerpFactor;
      currentPosRef.current.y += (targetPosRef.current.y - currentPosRef.current.y) * lerpFactor;
      setTrailingPos({
        x: currentPosRef.current.x,
        y: currentPosRef.current.y,
      });
      requestRef.current = requestAnimationFrame(animate);
    };
    requestRef.current = requestAnimationFrame(animate);

    return () => {
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('mousedown', handleMouseDown);
      window.removeEventListener('mouseup', handleMouseUp);
      document.removeEventListener('mouseleave', handleMouseLeave);
      document.removeEventListener('mouseenter', handleMouseEnter);
      if (requestRef.current) cancelAnimationFrame(requestRef.current);
    };
  }, [isVisible]);

  if (isTouchDevice || !isVisible) return null;

  return (
    <>
      {/* Outer Luminous Amethyst Halo / Trailing Ring */}
      <div
        style={{
          transform: `translate3d(${trailingPos.x}px, ${trailingPos.y}px, 0) translate(-50%, -50%) scale(${
            isClicked ? 0.8 : isHovered ? 1.6 : 1
          })`,
        }}
        className={`fixed top-0 left-0 pointer-events-none z-50 rounded-full transition-transform duration-75 ease-out will-change-transform ${
          isHovered
            ? 'w-10 h-10 border border-purple-400/80 bg-purple-500/20 shadow-[0_0_20px_rgba(168,85,247,0.5)] backdrop-blur-[1px]'
            : 'w-7 h-7 border border-purple-500/40 bg-purple-600/10 shadow-[0_0_12px_rgba(168,85,247,0.25)]'
        }`}
      />

      {/* Floating Center Violet Spark Dot */}
      <div
        style={{
          transform: `translate3d(${position.x}px, ${position.y}px, 0) translate(-50%, -50%) scale(${
            isClicked ? 1.4 : isHovered ? 0.7 : 1
          })`,
        }}
        className="fixed top-0 left-0 pointer-events-none z-50 w-2 h-2 rounded-full bg-purple-200 shadow-[0_0_8px_rgba(232,121,249,0.9)] transition-transform duration-75 ease-out will-change-transform"
      />
    </>
  );
}
