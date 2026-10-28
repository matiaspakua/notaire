import Link from "next/link";
import { DOCS_PAGES, FACTORY_ACTS, REPORTS, REPO_URL } from "@/lib/site";
import { GitHubIcon, Logo } from "./icons";

/** The one site footer (#1970). */
export function SiteFooter({ version }: { version?: string }) {
  const cols = [
    { title: "AI Factory", links: FACTORY_ACTS.map((a) => ({ href: a.pages[0].href, label: a.label.replace(/^[IVX]+ · /, "") })) },
    { title: "Technical docs", links: DOCS_PAGES.slice(0, 6).map((p) => ({ href: p.href, label: p.title })) },
    { title: "Reports", links: [{ href: "/docs/releases/", label: "Releases" }, ...REPORTS.map((r) => ({ href: r.href, label: r.title }))] },
  ];
  return (
    <footer className="border-t border-slate-200 bg-slate-50">
      <div className="mx-auto grid max-w-7xl gap-10 px-4 py-12 sm:px-6 md:grid-cols-[1.4fr_repeat(3,1fr)]">
        <div className="space-y-3">
          <Link href="/" className="inline-flex items-center gap-2.5">
            <Logo id="logo-footer" />
            <span className="font-semibold text-slate-900">Notaire</span>
          </Link>
          <p className="max-w-xs text-sm text-slate-600">
            A notarial management system, and the AI-assisted software factory that builds it. 2014 – 2026
            {version ? ` · v${version}` : ""}.
          </p>
          <a href={REPO_URL} className="inline-flex items-center gap-2 text-sm font-medium text-slate-700 hover:text-slate-900">
            <GitHubIcon className="h-4 w-4" /> matiaspakua/notaire
          </a>
        </div>
        {cols.map((c) => (
          <div key={c.title}>
            <h2 className="mb-3 text-xs font-semibold uppercase tracking-wider text-slate-500">{c.title}</h2>
            <ul className="space-y-2 text-sm">
              {c.links.map((l) => (
                <li key={l.href + l.label}>
                  <Link href={l.href} prefetch={false} className="text-slate-700 hover:text-slate-900 hover:underline">
                    {l.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
      <p className="border-t border-slate-200 px-4 py-5 text-center text-xs text-slate-600">
        Every claim on this site links its source in the repository. Numbers are read from it when the site is built.
      </p>
    </footer>
  );
}
