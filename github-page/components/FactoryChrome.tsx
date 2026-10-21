"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { FACTORY_NAV } from "@/lib/ai-sdlc-content";

/** Chrome for the AI Factory section (#1934): visibly apart from the product Docs tab. */
export function FactoryChrome({ children }: { children: React.ReactNode }) {
  const pathname = usePathname() || "";

  return (
    <div className="min-h-screen bg-slate-50" style={{ cursor: "auto" }}>
      <header className="sticky top-0 z-40 border-b border-slate-800 bg-slate-950/95 text-slate-100 backdrop-blur">
        <div className="max-w-6xl mx-auto px-6 py-4 flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <Link href="/ai-sdlc/" className="flex items-center gap-2">
              <span className="w-8 h-8 rounded-lg bg-gradient-to-br from-violet-500 via-fuchsia-500 to-amber-400 flex items-center justify-center text-[11px] font-bold text-white">
                AI
              </span>
              <span className="text-sm font-semibold tracking-wide">AI FACTORY</span>
            </Link>
            <span className="hidden sm:inline text-xs text-slate-400">the AI SDLC that builds Notaire</span>
          </div>
          <div className="flex items-center gap-4 text-sm">
            <Link href="/docs/" className="text-slate-300 hover:text-white transition-colors">
              Product docs →
            </Link>
            <Link href="/" className="text-slate-300 hover:text-white transition-colors">
              Story site
            </Link>
          </div>
        </div>
        <nav className="max-w-6xl mx-auto px-6 pb-3 flex flex-wrap gap-1" aria-label="AI Factory sections">
          {FACTORY_NAV.map((l) => {
            const active =
              l.href === "/ai-sdlc/"
                ? pathname === "/ai-sdlc" || pathname === "/ai-sdlc/"
                : pathname.startsWith(l.href.replace(/\/$/, ""));
            return (
              <Link
                key={l.href}
                href={l.href}
                className={`px-3 py-1.5 rounded-full text-sm font-medium transition-colors ${
                  active
                    ? "bg-violet-500/20 text-violet-200 border border-violet-400/40"
                    : "text-slate-300 hover:bg-white/10"
                }`}
              >
                {l.label}
              </Link>
            );
          })}
        </nav>
      </header>
      <main className="max-w-6xl mx-auto px-6 py-10">{children}</main>
      <footer className="border-t border-slate-200 py-8 text-center text-xs text-slate-500">
        The AI Factory is the process. Notaire is the product it builds. Every claim on these pages links its source in{" "}
        <a className="underline hover:text-violet-700" href="https://github.com/matiaspakua/notaire">
          matiaspakua/notaire
        </a>
        .
      </footer>
    </div>
  );
}
