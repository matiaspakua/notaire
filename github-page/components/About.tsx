import type { AboutData } from "@/lib/about";

/** The sourced facts from lib/about.json (#1985); placeholders are never rendered. */
export function AboutFacts({ data }: { data: Pick<AboutData, "facts" | "checked"> & { placeholders?: unknown } }) {
  return (
    <div>
      <dl className="divide-y divide-slate-200 rounded-2xl border border-slate-200 bg-white shadow-[var(--shadow-card)]">
        {data.facts.map((f) => (
          <div key={f.label} className="grid gap-1 px-5 py-4 sm:grid-cols-[12rem_1fr] sm:gap-6">
            <dt className="text-xs font-medium uppercase tracking-wider text-slate-600 sm:pt-1">{f.label}</dt>
            <dd>
              <p className="leading-relaxed text-slate-900">{f.href ? <a href={f.href}>{f.value}</a> : f.value}</p>
              <p className="mt-1 text-xs text-slate-600">
                Source: <a href={f.source.href}>{f.source.label}</a>
              </p>
            </dd>
          </div>
        ))}
      </dl>
      <p className="mt-3 text-xs text-slate-600">Sources checked {data.checked}.</p>
    </div>
  );
}
