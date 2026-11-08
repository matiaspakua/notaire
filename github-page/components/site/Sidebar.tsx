import Link from "next/link";
import type { PageGroup } from "@/lib/site";

export function Sidebar({ label, groups, pathname, footer }: { label: string; groups: PageGroup[]; pathname: string; footer?: React.ReactNode }) {
  return (
    <nav aria-label={label} className="text-sm">
      {groups.map((g) => (
        <div key={g.label} className="mb-6">
          <p className="mb-2 px-3 text-[11px] font-semibold uppercase tracking-wider text-slate-500">{g.label}</p>
          <ul className="space-y-0.5 border-l border-slate-200">
            {g.pages.map((p) => {
              const current = p.href === pathname;
              return (
                <li key={p.href}>
                  <Link
                    href={p.href}
                    aria-current={current ? "page" : undefined}
                    className={`-ml-px block border-l-2 py-1.5 pl-3 pr-2 transition-colors ${
                      current
                        ? "border-[var(--accent-600)] font-semibold text-[var(--accent-700)]"
                        : "border-transparent text-slate-600 hover:border-slate-400 hover:text-slate-900"
                    }`}
                  >
                    {p.title}
                  </Link>
                </li>
              );
            })}
          </ul>
        </div>
      ))}
      {footer}
    </nav>
  );
}
