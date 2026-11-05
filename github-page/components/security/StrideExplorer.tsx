"use client";

import { useState } from "react";
import { STATUS_STYLE, STRIDE, type StrideItem } from "@/lib/security-content";

function Badge({ status }: { status: StrideItem["status"] }) {
  return <span className={`rounded-full px-2 py-0.5 text-[11px] font-semibold ${STATUS_STYLE[status]}`}>{status}</span>;
}

/** Click or arrow-key through STRIDE. The list is the accessible version of the diagram. */
export function StrideExplorer() {
  const [index, setIndex] = useState(0);
  const current = STRIDE[index];
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-[var(--shadow-card)] sm:p-6">
      <div role="tablist" aria-label="STRIDE categories" className="flex flex-wrap gap-2">
        {STRIDE.map((item, i) => {
          const selected = i === index;
          return (
            <button
              key={item.letter}
              type="button"
              role="tab"
              id={`stride-tab-${item.letter}`}
              aria-selected={selected}
              aria-controls="stride-panel"
              tabIndex={selected ? 0 : -1}
              onClick={() => setIndex(i)}
              onKeyDown={(event) => {
                if (event.key !== "ArrowRight" && event.key !== "ArrowLeft") return;
                event.preventDefault();
                const next = event.key === "ArrowRight" ? (i + 1) % STRIDE.length : (i - 1 + STRIDE.length) % STRIDE.length;
                setIndex(next);
                document.getElementById(`stride-tab-${STRIDE[next].letter}`)?.focus();
              }}
              className={`rounded-full border px-3 py-1.5 text-sm font-semibold ${
                selected ? "border-[var(--accent-600)] bg-[var(--accent-50)] text-[var(--accent-700)]" : "border-slate-200 text-slate-700 hover:border-slate-400"
              }`}
            >
              <span className="font-mono">{item.letter}</span> {item.name}
            </button>
          );
        })}
      </div>
      <div role="tabpanel" id="stride-panel" aria-labelledby={`stride-tab-${current.letter}`} className="mt-5">
        <h3 className="font-display text-2xl font-semibold text-slate-900">{current.name}</h3>
        <p className="mt-2 max-w-3xl text-slate-700">{current.question}</p>
        <ul className="mt-4 space-y-3">
          {current.items.map((item) => (
            <li key={item.id} className="rounded-xl border border-slate-200 px-4 py-3">
              <div className="flex flex-wrap items-center gap-2">
                <span className="font-mono text-sm font-semibold text-slate-900">{item.id}</span>
                <Badge status={item.status} />
              </div>
              <p className="mt-1 text-sm leading-relaxed text-slate-700">{item.summary}</p>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
