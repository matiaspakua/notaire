"use client";

import { useState } from "react";
import { tr } from "@/lib/i18n/es";
import "@/lib/i18n/strings";
import { CADENCE, TRIGGER_LABEL, type CadenceTrigger } from "@/lib/security-content";
import { usePreferences } from "@/components/site/Preferences";

const FILTERS: { id: "all" | CadenceTrigger; label: string }[] = [
  { id: "all", label: "All" },
  { id: "local", label: "Local" },
  { id: "release", label: "Version tag" },
  { id: "release-schedule", label: "Tag and weekly" },
  { id: "schedule", label: "Weekly" },
  { id: "main", label: "Main" },
  { id: "platform", label: "GitHub app" },
];

/** Filter the public tool cadence. Workflow names are the files in .github/workflows. */
export function CadenceBoard() {
  const { locale } = usePreferences();
  const [filter, setFilter] = useState<(typeof FILTERS)[number]["id"]>("all");
  const rows = CADENCE.entries.filter((entry) => {
    if (filter === "all") return true;
    if (filter === "local") return Boolean(entry.local) || entry.trigger === "local";
    if (filter === "release") return entry.trigger === "release" || entry.trigger === "release-schedule";
    return entry.trigger === filter;
  });
  return (
    <div>
      <div role="group" aria-label={tr(locale, "Filter tools by when they run")} className="flex flex-wrap gap-2">
        {FILTERS.map((item) => {
          const pressed = filter === item.id;
          return (
            <button
              key={item.id}
              type="button"
              aria-pressed={pressed}
              onClick={() => setFilter(item.id)}
              className={`rounded-full border px-3 py-1.5 text-sm font-medium ${
                pressed ? "border-[var(--accent-600)] bg-[var(--accent-50)] text-[var(--accent-700)]" : "border-slate-200 text-slate-700 hover:border-slate-400"
              }`}
            >
              {tr(locale, item.label)}
            </button>
          );
        })}
      </div>
      <p className="mt-3 text-sm text-slate-600">{tr(locale, CADENCE.disclaimer)}</p>
      <div className="mt-4 overflow-x-auto rounded-2xl border border-slate-200" tabIndex={0}>
        <table className="min-w-full text-sm">
          <thead className="bg-slate-50 text-left text-slate-600">
            <tr>
              <th className="px-4 py-3 font-medium">{tr(locale, "Tool")}</th>
              <th className="px-4 py-3 font-medium">{tr(locale, "When")}</th>
              <th className="px-4 py-3 font-medium">{tr(locale, "Where")}</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.id} className="border-t border-slate-100 align-top">
                <th scope="row" className="px-4 py-3 text-left font-semibold text-slate-900">
                  {row.tool}
                  <div className="font-mono text-[11px] font-normal text-slate-500">{row.workflow ?? row.local}</div>
                </th>
                <td className="px-4 py-3 text-slate-700">{tr(locale, TRIGGER_LABEL[row.trigger])}</td>
                <td className="px-4 py-3 text-slate-700">{tr(locale, row.where)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
