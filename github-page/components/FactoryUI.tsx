import type { ReactNode } from "react";
import type { Source } from "@/lib/ai-sdlc-content";
import { MermaidDiagram } from "@/components/MermaidDiagram";

export function PageHeader({ eyebrow, title, lead }: { eyebrow: string; title: string; lead: ReactNode }) {
  return (
    <header className="space-y-4 mb-10">
      <p className="text-xs uppercase tracking-[0.2em] text-violet-600 font-semibold">{eyebrow}</p>
      <h1 className="text-4xl sm:text-5xl font-semibold tracking-tight text-slate-900" style={{ fontFamily: "var(--font-display)" }}>
        {title}
      </h1>
      <div className="text-lg text-slate-600 max-w-3xl leading-relaxed">{lead}</div>
    </header>
  );
}

export function SectionTitle({ children, id }: { children: ReactNode; id?: string }) {
  return (
    <h2 id={id} className="text-2xl font-semibold text-slate-900 mt-14 mb-4 scroll-mt-32" style={{ fontFamily: "var(--font-display)" }}>
      {children}
    </h2>
  );
}

const FACTORY_FLOWCHART = { wrappingWidth: 420, nodeSpacing: 40, rankSpacing: 50 };

export function Diagram({ chart, caption }: { chart: string; caption: string }) {
  return (
    <figure className="my-6">
      <MermaidDiagram chart={chart} flowchart={FACTORY_FLOWCHART} className="overflow-x-auto rounded-2xl border border-slate-200 bg-white p-4 shadow-sm [&_svg]:mx-auto" />
      <figcaption className="mt-2 text-xs text-slate-500 text-center">{caption}</figcaption>
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
          className="inline-flex items-center gap-1 rounded-full border border-slate-200 bg-slate-50 px-2.5 py-0.5 font-mono text-[11px] text-slate-600 hover:border-violet-400 hover:text-violet-700"
        >
          ↗ {s.label}
        </a>
      ))}
    </div>
  );
}

export function Card({ children, className = "" }: { children: ReactNode; className?: string }) {
  return <div className={`rounded-2xl border border-slate-200 bg-white p-5 shadow-sm ${className}`}>{children}</div>;
}

export function Code({ children }: { children: ReactNode }) {
  return <code className="rounded bg-slate-100 px-1.5 py-0.5 font-mono text-[12px] text-slate-800">{children}</code>;
}

export function Table({ head, rows }: { head: string[]; rows: ReactNode[][] }) {
  return (
    <div className="overflow-x-auto rounded-2xl border border-slate-200 bg-white shadow-sm">
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
