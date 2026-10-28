"use client";

import { useEffect, useRef } from "react";

/** A thin bar under the header that fills as the reader scrolls (#1970). Decorative. */
export function ReadingProgress() {
  const bar = useRef<HTMLDivElement>(null);
  useEffect(() => {
    let frame = 0;
    function update() {
      frame = 0;
      const el = document.documentElement;
      const max = el.scrollHeight - el.clientHeight;
      const ratio = max > 0 ? Math.min(1, Math.max(0, el.scrollTop / max)) : 0;
      if (bar.current) bar.current.style.transform = `scaleX(${ratio})`;
    }
    function onScroll() {
      if (!frame) frame = requestAnimationFrame(update);
    }
    update();
    window.addEventListener("scroll", onScroll, { passive: true });
    window.addEventListener("resize", onScroll);
    return () => {
      window.removeEventListener("scroll", onScroll);
      window.removeEventListener("resize", onScroll);
      cancelAnimationFrame(frame);
    };
  }, []);
  return (
    <div data-reading-progress aria-hidden="true" className="pointer-events-none fixed inset-x-0 top-[var(--header-h)] z-40 h-0.5">
      <div ref={bar} className="h-full origin-left bg-[var(--accent-600)]" style={{ transform: "scaleX(0)" }} />
    </div>
  );
}
