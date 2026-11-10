"use client";

import { useId, useState } from "react";
import { tf, tr } from "@/lib/i18n/es";
import "@/lib/i18n/strings";
import { usePreferences } from "@/components/site/Preferences";

export type WeekPoint = { week: string; merged: number };

/**
 * Weekly throughput from delivery-metrics.json (#2202 follow-on). The series is the snapshot;
 * this component does not invent a week or a count.
 */
export function DeliveryWeeks({ series, medianPerWeek }: { series: WeekPoint[]; medianPerWeek: number | null }) {
  const { locale } = usePreferences();
  const text = (value: string) => tr(locale, value);
  const labelId = useId();
  const [active, setActive] = useState(series.length - 1);
  const max = Math.max(1, ...series.map((point) => point.merged));
  const current = series[active];

  if (series.length === 0) {
    return <p className="text-sm text-slate-600">{text("This snapshot has no weekly counts.")}</p>;
  }

  const w = 720;
  const h = 220;
  const padL = 36;
  const padR = 12;
  const padT = 16;
  const padB = 36;
  const innerW = w - padL - padR;
  const innerH = h - padT - padB;
  const slot = innerW / series.length;
  const barW = Math.min(28, slot * 0.62);

  return (
    <figure className="delivery-weeks my-8 rounded-2xl border border-slate-200 bg-white p-4 text-slate-900 shadow-sm" aria-labelledby={labelId}>
      <figcaption id={labelId} className="mb-3 text-sm leading-relaxed text-slate-700">
        {text("Merged pull requests each ISO week")}
        {". "}
        {tf(locale, "Median {median} per week. A week with no merge is zero. Bot pull requests are excluded.", {
          median: medianPerWeek ?? "n/a",
        })}
      </figcaption>
      <div className="overflow-x-auto">
        <svg viewBox={`0 0 ${w} ${h}`} className="h-auto min-w-[36rem] w-full" aria-hidden="true">
          {[0, 0.5, 1].map((step) => {
            const value = Math.round(max * step);
            const y = padT + innerH - (value / max) * innerH;
            return (
              <g key={step}>
                <line x1={padL} x2={w - padR} y1={y} y2={y} stroke="#e2e8f0" />
                <text x={padL - 6} y={y + 4} textAnchor="end" fontSize={11} fill="#475569">
                  {value}
                </text>
              </g>
            );
          })}
          {series.map((point, index) => {
            const height = (point.merged / max) * innerH;
            const x = padL + slot * index + (slot - barW) / 2;
            const y = padT + innerH - height;
            const on = index === active;
            return (
              <g key={point.week}>
                <rect className="delivery-week-bar" x={x} y={y} width={barW} height={Math.max(height, 1)} rx={3} fill={on ? "#6d28d9" : "#93c5fd"} />
                <text x={x + barW / 2} y={h - 8} textAnchor="middle" fontSize={10} fill="#334155">
                  {point.week.slice(5)}
                </text>
              </g>
            );
          })}
        </svg>
      </div>
      <div className="mt-3 flex flex-wrap gap-1" role="listbox" aria-label={text("Weeks")}>
        {series.map((point, index) => (
          <button
            key={point.week}
            type="button"
            role="option"
            aria-selected={index === active}
            onClick={() => setActive(index)}
            className={`rounded-full border px-2.5 py-1 font-mono text-[11px] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#6d28d9] ${
              index === active ? "border-[#6d28d9] bg-[#ede9fe] text-[#4c1d95]" : "border-slate-200 text-slate-700"
            }`}
          >
            {point.week}
          </button>
        ))}
      </div>
      <p className="mt-2 text-sm text-slate-800" aria-live="polite">
        {current ? tf(locale, "{week}: {count} merged pull requests", { week: current.week, count: current.merged }) : ""}
      </p>
      <table className="mt-3 w-full text-left text-sm">
        <caption className="sr-only">{text("Weekly counts from the delivery snapshot")}</caption>
        <thead>
          <tr className="text-slate-500">
            <th className="py-1 font-medium">{text("Week")}</th>
            <th className="py-1 font-medium">{text("Merged pull requests")}</th>
          </tr>
        </thead>
        <tbody>
          {series.map((point) => (
            <tr key={point.week} className="border-t border-slate-100">
              <td className="py-1 font-mono text-xs">{point.week}</td>
              <td className="py-1 tabular-nums">{point.merged}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </figure>
  );
}
