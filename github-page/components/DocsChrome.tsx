"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const links = [
  { href: "/docs/", label: "Overview" },
  { href: "/docs/business/", label: "Business" },
  { href: "/docs/modules/", label: "Modules" },
  { href: "/docs/architecture/", label: "Architecture" },
  { href: "/docs/testing/", label: "Testing" },
  { href: "/docs/security/", label: "Security" },
];

export function DocsChrome({ children }: { children: React.ReactNode }) {
  const pathname = usePathname() || "";

  return (
    <div className="min-h-screen" style={{ background: "linear-gradient(180deg, #f5f7fb 0%, #ffffff 40%)", cursor: "auto" }}>
      <header className="sticky top-0 z-40 border-b border-neutral-200/80 bg-white/90 backdrop-blur">
        <div className="max-w-5xl mx-auto px-6 py-4 flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <Link href="/" className="flex items-center gap-2 group">
              <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-cyan-500 to-blue-600 flex items-center justify-center text-white text-xs font-bold">
                N
              </div>
              <span className="text-sm font-semibold text-neutral-900">NOTAIRE</span>
            </Link>
            <span className="text-neutral-300">/</span>
            <span className="text-sm text-neutral-600">Technical Docs</span>
          </div>
          <Link
            href="/"
            className="text-sm text-neutral-600 hover:text-[#0A84FF] transition-colors"
          >
            ← Story site
          </Link>
        </div>
        <nav className="max-w-5xl mx-auto px-6 pb-3 flex flex-wrap gap-1">
          {links.map((l) => {
            const active =
              l.href === "/docs/"
                ? pathname === "/docs" || pathname === "/docs/"
                : pathname.startsWith(l.href.replace(/\/$/, ""));
            return (
              <Link
                key={l.href}
                href={l.href}
                className={`px-3 py-1.5 rounded-full text-sm font-medium transition-colors ${
                  active
                    ? "bg-cyan-500/15 text-[#0A84FF] border border-cyan-500/30"
                    : "text-neutral-600 hover:bg-neutral-100"
                }`}
              >
                {l.label}
              </Link>
            );
          })}
        </nav>
      </header>
      <main className="max-w-5xl mx-auto px-6 py-10">{children}</main>
    </div>
  );
}
