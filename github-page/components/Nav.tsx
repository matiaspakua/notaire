"use client";
import { useEffect, useState } from "react";

const links = [
  { label: "Origin", href: "#origin" },
  { label: "Evolution", href: "#evolution" },
  { label: "Architecture", href: "#architecture" },
  { label: "AI Era", href: "#ai" },
  { label: "Today", href: "#today" },
];

export function Nav() {
  const [scrolled, setScrolled] = useState(false);
  const [active, setActive] = useState("");

  useEffect(() => {
    const onScroll = () => {
      setScrolled(window.scrollY > 60);
      const sections = document.querySelectorAll("section[id]");
      sections.forEach(sec => {
        const rect = sec.getBoundingClientRect();
        if (rect.top <= 120 && rect.bottom > 120) setActive(sec.id);
      });
    };
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  return (
    <nav className={`fixed top-0 left-0 right-0 z-50 transition-all duration-500 ${
      scrolled ? "py-3 glass border-b border-white/5 shadow-2xl" : "py-6"
    }`}>
      <div className="max-w-7xl mx-auto px-6 flex items-center justify-between">
        <a href="#" className="flex items-center gap-3 group">
          <div className="relative w-9 h-9">
            <div className="absolute inset-0 rounded-lg bg-gradient-to-br from-cyan-500 to-purple-600 opacity-80 group-hover:opacity-100 transition-opacity" />
            <div className="absolute inset-0 flex items-center justify-center text-white font-bold text-sm" style={{ fontFamily: "var(--font-mono)" }}>N</div>
          </div>
          <span className="font-semibold text-white text-sm tracking-wide hidden sm:block">NOTAIRE</span>
        </a>

        <div className="flex items-center gap-1">
          {links.map(l => (
            <a
              key={l.href}
              href={l.href}
              className={`px-4 py-1.5 rounded-full text-sm font-medium transition-all duration-200 ${
                active === l.href.slice(1)
                  ? "bg-cyan-500/20 text-cyan-400 border border-cyan-500/30"
                  : "text-slate-400 hover:text-white hover:bg-white/5"
              }`}
            >
              {l.label}
            </a>
          ))}
          <a
            href="https://github.com/matiaspakua/notaire"
            target="_blank"
            rel="noopener noreferrer"
            className="ml-3 flex items-center gap-2 px-4 py-1.5 rounded-full bg-white/5 border border-white/10 text-sm text-white hover:bg-white/10 transition-all"
          >
            <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 0C5.37 0 0 5.37 0 12c0 5.3 3.438 9.8 8.205 11.387.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 21.795 24 17.295 24 12c0-6.63-5.37-12-12-12"/>
            </svg>
            GitHub
          </a>
        </div>
      </div>
    </nav>
  );
}
