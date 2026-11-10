"use client";

import type { FactoryNow } from "@/lib/factory-status";
import { usePreferences } from "@/components/site/Preferences";

export function FactoryNowView({ model }: { model: FactoryNow }) {
  const { locale } = usePreferences();
  const copy = model[locale];
  const shipped = [copy.config, copy.grant, copy.isolator, copy.foreman, copy.leadTime];
  return (
    <section aria-labelledby="factory-now-title" className="mt-10">
      <h2 id="factory-now-title" className="font-display text-3xl font-semibold text-slate-900">
        {copy.title}
      </h2>
      <p className="mt-3 max-w-3xl leading-relaxed text-slate-700">{copy.lead}</p>
      <div className="mt-6 grid gap-4 lg:grid-cols-2">
        <article className="rounded-2xl border border-slate-200 bg-white p-5">
          <h3 className="font-semibold text-slate-900">{copy.shippedTitle}</h3>
          <ul className="mt-3 space-y-3 text-sm leading-relaxed text-slate-700">
            {shipped.map((item) => (
              <li key={item}>{item}</li>
            ))}
          </ul>
        </article>
        <article className="rounded-2xl border border-slate-200 bg-white p-5">
          <h3 className="font-semibold text-slate-900">{copy.plannedTitle}</h3>
          <p className="mt-3 text-sm leading-relaxed text-slate-700">{copy.approval}</p>
          <p className="mt-3 text-sm leading-relaxed text-slate-700">
            {copy.counts}: {model.counts.migrated} migrated · {model.counts.planned} planned · {model.counts.stay} stay.
          </p>
          <ul className="mt-3 space-y-1 font-mono text-xs text-slate-700">
            {model.plannedSources.map((source) => (
              <li key={source}>{source}</li>
            ))}
          </ul>
        </article>
      </div>
      <p className="mt-4 text-sm text-slate-700">
        <span className="font-semibold text-slate-900">{copy.loaded}: </span>
        {model.loadedPlugins.join(", ")}. {copy.loadedNote}
      </p>
    </section>
  );
}
