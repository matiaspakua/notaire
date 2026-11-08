import type { ReactNode } from "react";

/** A row of headline numbers (#1976). Values come from `lib/tech-facts.ts`, never typed in. */
export function StatGrid({ items }: { items: { value: ReactNode; label: string; hint?: string }[] }) {
  return (
    <dl className="my-8 grid grid-cols-2 gap-3 sm:grid-cols-4">
      {items.map((s) => (
        <div key={s.label} className="rounded-2xl border border-slate-200 bg-white p-4 shadow-[var(--shadow-card)]">
          <dt className="text-xs font-medium uppercase tracking-wider text-slate-500">{s.label}</dt>
          <dd className="mt-1 font-display text-3xl font-semibold tracking-tight text-slate-900">{s.value}</dd>
          {s.hint && <dd className="mt-1 text-xs text-slate-500">{s.hint}</dd>}
        </div>
      ))}
    </dl>
  );
}

/** A labelled technology chip with its version (or "—" when it could not be read). */
export function Tech({ name, version }: { name: string; version?: string | null }) {
  return (
    <span className="inline-flex items-center gap-1.5 rounded-full border border-slate-200 bg-white px-3 py-1 text-sm text-slate-700">
      <span className="font-medium text-slate-900">{name}</span>
      {version !== undefined && <span className="font-mono text-xs text-slate-500">{version ?? "—"}</span>}
    </span>
  );
}

export function Callout({ title, children }: { title: string; children: ReactNode }) {
  return (
    <aside className="my-6 rounded-2xl border border-[var(--accent-500)]/30 bg-[var(--accent-500)]/5 p-5">
      <p className="font-semibold text-slate-900">{title}</p>
      <div className="mt-1 text-slate-700">{children}</div>
    </aside>
  );
}
