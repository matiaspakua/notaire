import type { ReactNode } from "react";
import type { Chapter, Snippet } from "@/lib/ai-sdlc-content";
import { issue } from "@/lib/ai-sdlc-content";
import { Sources } from "@/components/FactoryUI";
import history from "@/lib/ai-sdlc-history.json";

/** Case-study building blocks (#1958): chapters, before/after excerpts, terminal panes, history chart. */

type Month = { month: string; commits: number; merged_prs: number; process_prs: number; bug_issues: number };
const MONTH_NAMES = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

export const HISTORY = history as {
  generated_at: string;
  generated_by: string;
  legacy_commits: number;
  totals: { commits: number; merged_prs: number; merged_prs_without_bots: number; issues: number; bug_issues: number };
  months: Month[];
  mechanisms: { date: string; label: string; issue: number | null; path: string }[];
};

function monthLabel(m: string): string {
  return `${MONTH_NAMES[Number(m.slice(5, 7)) - 1]} ${m.slice(2, 4)}`;
}

export function HistoryChart({ from = "2026-03" }: { from?: string }) {
  const months = HISTORY.months.filter((m) => m.month >= from);
  const W = 760;
  const H = 360;
  const L = 48;
  const R = 52;
  const T = 24;
  const B = 56;
  const pw = W - L - R;
  const ph = H - T - B;
  const max = Math.ceil(Math.max(...months.map((m) => m.merged_prs)) / 100) * 100;
  const slot = pw / months.length;
  const bw = slot * 0.58;
  const y = (v: number) => T + ph - (v / max) * ph;
  const yr = (v: number) => T + ph - v * ph;
  const ratio = (m: Month) => (m.merged_prs ? m.bug_issues / m.merged_prs : 0);
  const cx = (i: number) => L + slot * i + slot / 2;
  const line = months.map((m, i) => `${cx(i).toFixed(1)},${yr(Math.min(ratio(m), 1)).toFixed(1)}`).join(" ");

  return (
    <figure className="my-6 max-w-4xl">
      <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
        <svg viewBox={`0 0 ${W} ${H}`} role="img" aria-labelledby="hc-title hc-desc" className="w-full h-auto">
          <title id="hc-title">Merged PRs, process PRs and bug issues per merged PR, by month</title>
          <desc id="hc-desc">
            {months.map((m) => `${monthLabel(m.month)}: ${m.merged_prs} merged PRs, ${m.process_prs} about the process, ${m.bug_issues} bug issues`).join("; ")}
          </desc>
          {Array.from({ length: max / 100 + 1 }, (_, k) => k * 100).map((v) => (
            <g key={v}>
              <line x1={L} x2={W - R} y1={y(v)} y2={y(v)} stroke="#e2e8f0" />
              <text x={L - 8} y={y(v) + 4} textAnchor="end" fontSize={11} fill="#475569">
                {v}
              </text>
            </g>
          ))}
          {[0, 0.25, 0.5, 0.75, 1].map((f) => (
            <text key={f} x={W - R + 8} y={yr(f) + 4} fontSize={11} fill="#b91c1c">
              {f.toFixed(2)}
            </text>
          ))}
          {months.map((m, i) => (
            <g key={m.month}>
              <rect x={cx(i) - bw / 2} y={y(m.merged_prs)} width={bw} height={y(0) - y(m.merged_prs)} rx={4} fill="#bfdbfe" />
              <rect x={cx(i) - bw / 2} y={y(m.process_prs)} width={bw} height={y(0) - y(m.process_prs)} rx={4} fill="#6d28d9" />
              <text x={cx(i)} y={T + ph + 18} textAnchor="middle" fontSize={12} fill="#334155">
                {monthLabel(m.month)}
              </text>
              <text x={cx(i)} y={T + ph + 34} textAnchor="middle" fontSize={11} fill="#64748b">
                {m.merged_prs} PRs
              </text>
            </g>
          ))}
          <polyline points={line} fill="none" stroke="#dc2626" strokeWidth={3} strokeLinejoin="round" />
          {months.map((m, i) => (
            <circle key={m.month} cx={cx(i)} cy={yr(Math.min(ratio(m), 1))} r={4.5} fill="#dc2626">
              <title>{`${monthLabel(m.month)}: ${ratio(m).toFixed(2)} bug issues per merged PR`}</title>
            </circle>
          ))}
        </svg>
        <div className="mt-2 flex flex-wrap gap-x-5 gap-y-1 text-xs text-slate-600">
          <span className="inline-flex items-center gap-1.5"><span className="h-3 w-3 rounded-sm bg-blue-200" aria-hidden /> merged PRs (no Dependabot)</span>
          <span className="inline-flex items-center gap-1.5"><span className="h-3 w-3 rounded-sm bg-violet-700" aria-hidden /> PRs about the process itself</span>
          <span className="inline-flex items-center gap-1.5"><span className="h-0.5 w-4 bg-red-600" aria-hidden /> bug issues opened per merged PR (right axis)</span>
        </div>
      </div>
      <details className="mt-2 text-sm">
        <summary className="cursor-pointer text-xs font-medium text-violet-700 hover:underline">Show the data as a table</summary>
        <div className="mt-2 overflow-x-auto rounded-xl border border-slate-200 bg-white" tabIndex={0} role="region" aria-label="Monthly delivery data">
          <table className="min-w-full text-xs">
            <caption className="sr-only">Monthly delivery data</caption>
            <thead className="bg-slate-50 text-left text-slate-600">
              <tr>
                {["Month", "Commits", "Merged PRs", "Process PRs", "Bug issues", "Bugs / PR"].map((h) => (
                  <th key={h} scope="col" className="px-3 py-2 font-medium">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {months.map((m) => (
                <tr key={m.month} className="border-t border-slate-100 tabular-nums">
                  <th scope="row" className="px-3 py-1.5 text-left font-medium text-slate-900">{m.month}</th>
                  <td className="px-3 py-1.5">{m.commits}</td>
                  <td className="px-3 py-1.5">{m.merged_prs}</td>
                  <td className="px-3 py-1.5">{m.process_prs}</td>
                  <td className="px-3 py-1.5">{m.bug_issues}</td>
                  <td className="px-3 py-1.5">{ratio(m).toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </details>
      <figcaption className="mt-2 text-xs text-slate-500">
        Snapshot generated {HISTORY.generated_at.slice(0, 10)} by <code className="font-mono">{HISTORY.generated_by}</code>.
        Correlation, not proof of causation: bug issues are counted by the month they were opened.
      </figcaption>
    </figure>
  );
}

export function Terminal({ title, lines, caption }: { title: string; lines: string[]; caption: string }) {
  return (
    <figure className="overflow-hidden rounded-2xl border border-slate-800 bg-slate-950 shadow-lg">
      <div className="flex items-center gap-2 border-b border-slate-800 px-4 py-2.5">
        <span className="h-2.5 w-2.5 rounded-full bg-red-400" aria-hidden />
        <span className="h-2.5 w-2.5 rounded-full bg-amber-400" aria-hidden />
        <span className="h-2.5 w-2.5 rounded-full bg-emerald-400" aria-hidden />
        <span className="ml-2 text-xs font-medium text-slate-300">{title}</span>
      </div>
      <pre className="overflow-x-auto px-4 py-4 font-mono text-[12.5px] leading-relaxed" tabIndex={0} aria-label={title}>
        {lines.map((l, i) => (
          <span key={i} className={`block ${tone(l)}`}>{l}</span>
        ))}
      </pre>
      <figcaption className="border-t border-slate-800 px-4 py-2 text-xs text-slate-400">{caption}</figcaption>
    </figure>
  );
}

function tone(line: string): string {
  if (line.startsWith("$")) return "text-slate-100";
  if (/FAIL|FAILED/.test(line)) return "text-red-300";
  if (/PASS|PASSED/.test(line)) return "text-emerald-300";
  if (/TODO/.test(line)) return "text-amber-300";
  return "text-slate-400";
}

function Excerpt({ snippet, tone: kind }: { snippet: Snippet; tone: "before" | "after" }) {
  const accent = kind === "before" ? "border-red-300 bg-red-50 text-red-800" : "border-emerald-300 bg-emerald-50 text-emerald-800";
  return (
    <figure className="flex min-w-0 flex-col overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
      <figcaption className={`border-b px-4 py-2 text-xs font-semibold ${accent}`}>
        <span className="uppercase tracking-wider">{kind}</span> · {snippet.label}
      </figcaption>
      <pre className="flex-1 overflow-x-auto bg-slate-50 px-4 py-3 font-mono text-[12px] leading-relaxed text-slate-800" tabIndex={0}>
        <code>{snippet.code}</code>
      </pre>
      <div className="px-4 pb-3">
        <Sources items={[snippet.source]} />
      </div>
    </figure>
  );
}

export function ChapterBlock({ chapter, index }: { chapter: Chapter; index: number }) {
  return (
    <section id={chapter.id} aria-labelledby={`${chapter.id}-title`} className="scroll-mt-40 border-t border-slate-200 py-12 first:border-t-0">
      <div className="grid gap-6 lg:grid-cols-[7rem_1fr]">
        <div className="flex items-baseline gap-3 lg:block">
          <span className="font-mono text-5xl font-semibold leading-none text-amber-600 lg:text-6xl" aria-hidden>
            {String(index + 1).padStart(2, "0")}
          </span>
          <p className="font-mono text-xs uppercase tracking-widest text-slate-500 lg:mt-3">{chapter.period}</p>
        </div>
        <div className="min-w-0">
          <h2 id={`${chapter.id}-title`} className="text-2xl sm:text-3xl font-semibold tracking-tight text-slate-900" style={{ fontFamily: "var(--font-display)" }}>
            <span className="sr-only">Chapter {index + 1}: </span>
            {chapter.title}
          </h2>
          <dl className="mt-6 grid gap-4 md:grid-cols-3">
            <Beat label="Problem" tone="text-red-700">{chapter.problem}</Beat>
            <Beat label="Move" tone="text-violet-700">{chapter.move}</Beat>
            <Beat label="Result" tone="text-emerald-700">{chapter.result}</Beat>
          </dl>
          {chapter.before && chapter.after && (
            <div className="mt-6 grid gap-4 md:grid-cols-2">
              <Excerpt snippet={chapter.before} tone="before" />
              <Excerpt snippet={chapter.after} tone="after" />
            </div>
          )}
          <div className="mt-5 flex flex-wrap items-center gap-2">
            {chapter.refs.map((n) => (
              <a key={n} href={issue(n)} className="rounded-full bg-slate-100 px-2.5 py-0.5 font-mono text-[11px] text-slate-700 hover:bg-violet-100 focus-visible:outline focus-visible:outline-2 focus-visible:outline-violet-600">
                #{n}
              </a>
            ))}
          </div>
          <Sources items={chapter.sources} />
        </div>
      </div>
    </section>
  );
}

function Beat({ label, tone, children }: { label: string; tone: string; children: ReactNode }) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
      <dt className={`text-[11px] font-semibold uppercase tracking-widest ${tone}`}>{label}</dt>
      <dd className="mt-2 text-sm leading-relaxed text-slate-700">{children}</dd>
    </div>
  );
}
