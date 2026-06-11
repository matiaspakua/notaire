"use client";
import { useEffect, useRef } from "react";
import { ParticleField } from "./ParticleField";

export function Hero() {
  const titleRef = useRef<HTMLHeadingElement>(null);
  const subtitleRef = useRef<HTMLParagraphElement>(null);
  const badgesRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const el = titleRef.current;
    if (!el) return;
    el.style.opacity = "0";
    el.style.transform = "translateY(40px)";
    setTimeout(() => {
      el.style.transition = "opacity 1s cubic-bezier(0.16,1,0.3,1), transform 1s cubic-bezier(0.16,1,0.3,1)";
      el.style.opacity = "1";
      el.style.transform = "translateY(0)";
    }, 200);

    const sub = subtitleRef.current;
    if (!sub) return;
    sub.style.opacity = "0";
    sub.style.transform = "translateY(30px)";
    setTimeout(() => {
      sub.style.transition = "opacity 1s cubic-bezier(0.16,1,0.3,1), transform 1s cubic-bezier(0.16,1,0.3,1)";
      sub.style.opacity = "1";
      sub.style.transform = "translateY(0)";
    }, 500);

    const badges = badgesRef.current;
    if (!badges) return;
    badges.style.opacity = "0";
    badges.style.transform = "translateY(20px)";
    setTimeout(() => {
      badges.style.transition = "opacity 0.8s, transform 0.8s";
      badges.style.opacity = "1";
      badges.style.transform = "translateY(0)";
    }, 800);
  }, []);

  return (
    <section id="top" className="relative min-h-screen flex items-center justify-center overflow-hidden grid-pattern">
      <ParticleField />
      {/* Radial glow */}
      <div className="absolute inset-0 pointer-events-none">
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[800px] h-[800px] rounded-full" style={{ background: "radial-gradient(circle, rgba(6,182,212,0.08) 0%, transparent 70%)" }} />
        <div className="absolute top-1/3 left-1/3 w-96 h-96 rounded-full blur-3xl" style={{ background: "rgba(124,58,237,0.06)" }} />
      </div>

      <div className="relative z-10 text-center px-6 max-w-5xl mx-auto">
        {/* Year badge */}
        <div ref={badgesRef} className="mb-8 flex justify-center gap-3 flex-wrap">
          <span className="tech-badge" style={{ background: "rgba(231,111,0,0.15)", color: "var(--java-orange)", border: "1px solid rgba(231,111,0,0.3)" }}>2014 → University Project</span>
          <span className="tech-badge" style={{ background: "rgba(6,182,212,0.1)", color: "var(--ai-cyan)", border: "1px solid rgba(6,182,212,0.25)" }}>2026 → AI-Powered</span>
        </div>

        <h1 ref={titleRef} className="mb-6 leading-none" style={{ fontFamily: "var(--font-display)", fontSize: "clamp(3.5rem, 10vw, 8rem)" }}>
          <span className="block text-white" style={{ letterSpacing: "-0.03em" }}>The Story of</span>
          <span className="block grad-cyan text-glow" style={{ letterSpacing: "-0.03em" }}>Notaire</span>
        </h1>

        <p ref={subtitleRef} className="text-slate-400 text-lg sm:text-xl max-w-2xl mx-auto leading-relaxed mb-10">
          From a Java Swing university project in 2014 to a modern,{" "}
          <span className="text-cyan-400 font-medium">AI-accelerated</span> microservices platform—
          a decade of evolution, one commit at a time.
        </p>

        <div className="flex items-center justify-center gap-6 flex-wrap">
          <a href="#origin" className="group flex items-center gap-2 px-8 py-3 rounded-full text-sm font-semibold transition-all duration-300"
            style={{ background: "linear-gradient(135deg, #06b6d4, #7c3aed)", color: "white" }}>
            Explore the Story
            <svg className="w-4 h-4 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
            </svg>
          </a>
          <a href="https://github.com/matiaspakua/notaire" target="_blank" rel="noopener noreferrer"
            className="flex items-center gap-2 px-8 py-3 rounded-full text-sm font-semibold text-slate-300 border border-slate-700 hover:border-slate-500 hover:text-white transition-all">
            <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"><path d="M12 0C5.37 0 0 5.37 0 12c0 5.3 3.438 9.8 8.205 11.387.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 21.795 24 17.295 24 12c0-6.63-5.37-12-12-12"/></svg>
            View on GitHub
          </a>
        </div>

        {/* Scroll indicator */}
        <div className="absolute bottom-12 left-1/2 -translate-x-1/2 flex flex-col items-center gap-2 text-slate-500 text-xs">
          <span>Scroll to discover</span>
          <div className="w-px h-12 bg-gradient-to-b from-cyan-500/50 to-transparent animate-pulse" />
        </div>
      </div>
    </section>
  );
}
