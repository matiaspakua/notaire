"use client";

import { tr } from "@/lib/i18n/es";
import "@/lib/i18n/strings";
import { usePreferences } from "@/components/site/Preferences";

/**
 * Median and p90 from delivery-metrics.json. The snapshot does not store every
 * pull request, so this is two marks on a scale, not a histogram.
 */
export function LeadTimeScale({ n, median, p90 }: { n: number; median: number | null; p90: number | null }) {
  const { locale } = usePreferences();
  const text = (value: string) => tr(locale, value);
  if (median === null || p90 === null || n === 0) {
    return <p className="text-sm text-slate-600">{text("Lead time was not measured in this snapshot.")}</p>;
  }
  const end = Math.max(p90 * 1.15, median + 0.1, 1);
  const at = (hours: number) => `${Math.min(100, (hours / end) * 100)}%`;
  const hours = (value: number) => `${value.toLocaleString("en-US", { maximumFractionDigits: 1 })} h`;

  return (
    <figure className="my-8 rounded-2xl border border-slate-200 bg-white p-5 text-slate-900 shadow-sm">
      <figcaption className="text-sm leading-relaxed text-slate-700">
        {text("Lead time from opened to merged.")}{" "}
        {text("The snapshot stores the median and the p90, not a histogram of every pull request.")}
      </figcaption>
      <div className="relative mt-10 h-3 rounded-full bg-slate-100" aria-hidden="true">
        <span className="absolute top-1/2 h-4 w-4 -translate-x-1/2 -translate-y-1/2 rounded-full border-2 border-white bg-[#0369a1]" style={{ left: at(median) }} />
        <span className="absolute top-1/2 h-4 w-4 -translate-x-1/2 -translate-y-1/2 rounded-full border-2 border-white bg-[#6d28d9]" style={{ left: at(p90) }} />
      </div>
      <div className="mt-3 flex justify-between font-mono text-[11px] text-slate-500" aria-hidden="true">
        <span>0 h</span>
        <span>{hours(end)}</span>
      </div>
      <table className="mt-4 w-full text-sm">
        <caption className="sr-only">{text("Lead time summary from the delivery snapshot")}</caption>
        <tbody>
          <tr className="border-t border-slate-100">
            <th scope="row" className="py-2 text-left font-medium text-slate-700">{text("Median")}</th>
            <td className="py-2 text-right font-mono tabular-nums">{hours(median)}</td>
          </tr>
          <tr className="border-t border-slate-100">
            <th scope="row" className="py-2 text-left font-medium text-slate-700">{text("p90")}</th>
            <td className="py-2 text-right font-mono tabular-nums">{hours(p90)}</td>
          </tr>
          <tr className="border-t border-slate-100">
            <th scope="row" className="py-2 text-left font-medium text-slate-700">{text("Pull requests in the window")}</th>
            <td className="py-2 text-right font-mono tabular-nums">{n.toLocaleString("en-US")}</td>
          </tr>
        </tbody>
      </table>
    </figure>
  );
}
