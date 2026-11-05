"use client";

import { useState } from "react";
import { CADENCE, POSTURE, STATUS_STYLE } from "@/lib/security-content";

/** Public snapshot. Selecting a row reveals the one-line status. It is not a scan result. */
export function PostureBoard() {
  const [id, setId] = useState(POSTURE[0].id);
  const current = POSTURE.find((item) => item.id === id) ?? POSTURE[0];
  return (
    <div className="grid gap-4 lg:grid-cols-[16rem_minmax(0,1fr)]">
      <div role="listbox" aria-label="Control families" className="space-y-2">
        {POSTURE.map((item) => {
          const selected = item.id === current.id;
          return (
            <button
              key={item.id}
              type="button"
              role="option"
              aria-selected={selected}
              onClick={() => setId(item.id)}
              className={`flex w-full items-center justify-between gap-3 rounded-xl border px-3 py-2 text-left text-sm ${
                selected ? "border-[var(--accent-600)] bg-[var(--accent-50)]" : "border-slate-200 bg-white hover:border-slate-400"
              }`}
            >
              <span className="font-semibold text-slate-900">{item.title}</span>
              <span className={`rounded-full px-2 py-0.5 text-[11px] font-semibold ${STATUS_STYLE[item.status]}`}>{item.status}</span>
            </button>
          );
        })}
      </div>
      <div aria-live="polite" className="rounded-2xl border border-slate-200 bg-white p-5">
        <p className="text-xs font-semibold uppercase tracking-[0.16em] text-slate-500">Snapshot {CADENCE.assessed}</p>
        <h3 className="mt-2 font-display text-2xl font-semibold text-slate-900">{current.title}</h3>
        <p className="mt-3 max-w-2xl leading-relaxed text-slate-700">{current.summary}</p>
        <p className="mt-4 text-sm text-slate-600">{CADENCE.disclaimer}</p>
      </div>
    </div>
  );
}
