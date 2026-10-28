import type { ReactNode } from "react";
import type { Source } from "@/lib/ai-sdlc-content";
import { MermaidDiagram } from "@/components/MermaidDiagram";

export function PageHeader({ eyebrow, title, lead }: { eyebrow: string; title: string; lead: ReactNode }) {
  return (
    <header className="mb-12 space-y-4">
      <p className="text-xs font-semibold uppercase tracking-[0.2em] text-[var(--accent-700)]">{eyebrow}</p>
      <h1 className="font-display text-4xl font-semibold leading-[1.1] tracking-tight text-slate-900 text-balance sm:text-5xl">{title}</h1>
      <div className="max-w-[65ch] text-lg leading-relaxed text-slate-600 sm:text-xl">{lead}</div>
    </header>
  );
}

export function SectionTitle({ children, id }: { children: ReactNode; id?: string }) {
  return (
    <h2 id={id} className="mb-4 mt-16 font-display text-2xl font-semibold tracking-tight text-slate-900 text-balance sm:text-3xl">
      {children}
    </h2>
  );
}

const FACTORY_FLOWCHART = { wrappingWidth: 420, nodeSpacing: 40, rankSpacing: 50 };

/** A themed Mermaid figure (#1970): named by its caption, scrolls sideways on phones when wide. */
export function Diagram({ chart, caption, wide = true }: { chart: string; caption: string; wide?: boolean }) {
  return (
    <figure className="my-8">
      <MermaidDiagram
        chart={chart}
        label={caption}
        wide={wide}
        flowchart={FACTORY_FLOWCHART}
        className="rounded-2xl border border-slate-200 bg-white p-4 shadow-[var(--shadow-card)] sm:p-6"
      />
      <figcaption className="mx-auto mt-3 text-center text-sm text-slate-600">
        {caption}
        {wide && <span className="block text-xs text-slate-500 sm:hidden">Swipe sideways to see the whole diagram.</span>}
      </figcaption>
    </figure>
  );
}

export function Sources({ items }: { items: Source[] }) {
  return (
    <div className="flex flex-wrap gap-2 mt-3">
      {items.map((s) => (
        <a
          key={s.href + s.label}
          href={s.href}
          className="inline-flex items-center gap-1 rounded-full border border-slate-200 bg-slate-50 px-2.5 py-0.5 font-mono text-[11px] text-slate-600 hover:border-[var(--accent-500)] hover:text-[var(--accent-700)]"
        >
          ↗ {s.label}
        </a>
      ))}
    </div>
  );
}

export function Card({ children, className = "" }: { children: ReactNode; className?: string }) {
  return <div className={`rounded-2xl border border-slate-200 bg-white p-5 shadow-[var(--shadow-card)] ${className}`}>{children}</div>;
}

export function Code({ children }: { children: ReactNode }) {
  return <code className="rounded bg-slate-100 px-1.5 py-0.5 font-mono text-[12px] text-slate-800">{children}</code>;
}

export function Table({ head, rows }: { head: string[]; rows: ReactNode[][] }) {
  return (
    <div className="overflow-x-auto rounded-2xl border border-slate-200 bg-white shadow-sm" tabIndex={0} role="region" aria-label={`Table: ${head.join(", ")}`}>
      <table className="min-w-full text-sm">
        <thead className="bg-slate-50 text-left text-slate-500">
          <tr>
            {head.map((h) => (
              <th key={h} className="px-4 py-3 font-medium">
                {h}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((r, i) => (
            <tr key={i} className="border-t border-slate-100 align-top">
              {r.map((c, j) => (
                <td key={j} className={`px-4 py-3 ${j === 0 ? "font-medium text-slate-900" : "text-slate-600"}`}>
                  {c}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
