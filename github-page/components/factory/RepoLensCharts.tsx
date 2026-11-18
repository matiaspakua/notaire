"use client";

import { useId, useState } from "react";
import { tr } from "@/lib/i18n/es";
import "@/lib/i18n/strings";
import { usePreferences } from "@/components/site/Preferences";
import {
  headlineFactors,
  kb,
  loadRepoLensBench,
  type AgentArm,
  type FactoryPair,
  type RetrievalArm,
} from "@/lib/repolens-bench";

type Bar = { key: string; label: string; value: number; className: string };

const ARM_CLASS: Record<string, string> = {
  grep: "fill-[var(--rl-grep)]",
  hybrid: "fill-[var(--rl-hybrid)]",
  graph: "fill-[var(--rl-graph)]",
  baseline: "fill-[var(--rl-grep)]",
  repolens: "fill-[var(--rl-graph)]",
};

function GroupBars({
  title,
  note,
  bars,
  format,
}: {
  title: string;
  note: string;
  bars: Bar[];
  format: (v: number) => string;
}) {
  const uid = useId();
  const max = Math.max(...bars.map((b) => b.value), 1);
  const h = 168;
  const top = 28;
  const bottom = 36;
  const left = 8;
  const width = Math.max(220, bars.length * 72);
  const plot = h - top - bottom;
  const slot = (width - 2 * left) / bars.length;
  const bw = Math.min(44, slot - 16);

  return (
    <figure className="rl-chart rounded-2xl border border-slate-200 bg-white p-4 text-slate-900 shadow-sm dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100">
      <figcaption className="text-sm font-semibold text-slate-900 dark:text-slate-100">{title}</figcaption>
      <p className="mt-1 text-xs leading-relaxed text-slate-600 dark:text-slate-400">{note}</p>
      <svg viewBox={`0 0 ${width} ${h}`} className="mt-3 h-auto w-full" role="img" aria-labelledby={`${uid}-t ${uid}-d`}>
        <title id={`${uid}-t`}>{title}</title>
        <desc id={`${uid}-d`}>{note}</desc>
        <line x1={left} x2={width - left} y1={top + plot} y2={top + plot} className="stroke-slate-200 dark:stroke-slate-700" />
        {bars.map((bar, i) => {
          const bh = (bar.value / max) * plot;
          const x = left + i * slot + (slot - bw) / 2;
          const y = top + plot - bh;
          return (
            <g key={bar.key}>
              <rect x={x} y={y} width={bw} height={Math.max(bh, 1)} rx={4} className={bar.className} />
              <text x={x + bw / 2} y={y - 6} textAnchor="middle" className="fill-slate-800 text-[10px] font-semibold dark:fill-slate-100">
                {format(bar.value)}
              </text>
              <text x={x + bw / 2} y={top + plot + 16} textAnchor="middle" className="fill-slate-500 text-[10px] dark:fill-slate-400">
                {bar.label}
              </text>
            </g>
          );
        })}
      </svg>
    </figure>
  );
}

function retrievalBars(arms: { grep: RetrievalArm; hybrid: RetrievalArm; graph: RetrievalArm }, metric: keyof RetrievalArm): Bar[] {
  return (["grep", "hybrid", "graph"] as const).map((key) => ({
    key,
    label: key,
    value: arms[key][metric],
    className: ARM_CLASS[key],
  }));
}

function agentBars(baseline: AgentArm, graph: AgentArm, metric: keyof Pick<AgentArm, "tool_calls" | "recall" | "duration_s" | "cost_usd">): Bar[] {
  return [
    { key: "baseline", label: "baseline", value: baseline[metric], className: ARM_CLASS.baseline },
    { key: "repolens", label: "+ RepoLens", value: graph[metric], className: ARM_CLASS.repolens },
  ];
}

function FactoryLoopTable({ pairs, caveat }: { pairs: FactoryPair[]; caveat: string }) {
  const { locale } = usePreferences();
  const text = (v: string) => tr(locale, v);
  const [filter, setFilter] = useState<"all" | "grep" | "repolens">("all");
  const rows = pairs.filter((p) => {
    if (filter === "all") return true;
    if (filter === "grep") return p.pass === "grep";
    return p.pass.startsWith("repolens");
  });

  return (
    <figure className="my-6 overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm dark:border-slate-700 dark:bg-slate-900">
      <figcaption className="border-b border-slate-100 px-4 py-3 text-sm text-slate-700 dark:border-slate-800 dark:text-slate-300">
        {text("Live factory-loop paired locate (grep vs RepoLens).")} {caveat}
      </figcaption>
      <div className="flex flex-wrap gap-2 px-4 py-3">
        {(
          [
            ["all", "All passes"],
            ["grep", "grep only"],
            ["repolens", "RepoLens only"],
          ] as const
        ).map(([id, label]) => (
          <button
            key={id}
            type="button"
            onClick={() => setFilter(id)}
            className={`rounded-full px-3 py-1 text-xs font-semibold transition-colors ${
              filter === id
                ? "bg-brand-700 text-white"
                : "border border-slate-300 bg-white text-slate-700 dark:border-slate-600 dark:bg-slate-800 dark:text-slate-200"
            }`}
            aria-pressed={filter === id}
          >
            {text(label)}
          </button>
        ))}
      </div>
      <div className="overflow-x-auto" tabIndex={0}>
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-700 dark:bg-slate-800 dark:text-slate-200">
            <tr>
              <th scope="col" className="px-3 py-2 font-semibold">{text("Issue")}</th>
              <th scope="col" className="px-3 py-2 font-semibold">{text("Order")}</th>
              <th scope="col" className="px-3 py-2 font-semibold">{text("Pass")}</th>
              <th scope="col" className="px-3 py-2 font-semibold">{text("Calls")}</th>
              <th scope="col" className="px-3 py-2 font-semibold">{text("Output bytes")}</th>
              <th scope="col" className="px-3 py-2 font-semibold">{text("Wall time")}</th>
              <th scope="col" className="px-3 py-2 font-semibold">{text("Hit rate")}</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={`${row.label}-${row.pass}-${row.wall}`} className="border-t border-slate-100 dark:border-slate-800">
                <th scope="row" className="px-3 py-2 font-medium text-slate-900 dark:text-slate-100">{row.label}</th>
                <td className="px-3 py-2 text-slate-700 dark:text-slate-300">{row.order}</td>
                <td className="px-3 py-2 font-mono text-xs text-slate-800 dark:text-slate-200">{row.pass}</td>
                <td className="px-3 py-2 font-mono tabular-nums">{row.calls}</td>
                <td className="px-3 py-2 font-mono tabular-nums">{row.output_bytes.toLocaleString("en-US")}</td>
                <td className="px-3 py-2 font-mono tabular-nums">{row.wall}</td>
                <td className="px-3 py-2 font-mono text-xs">{row.hit_rate}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </figure>
  );
}

export function RepoLensCharts() {
  const { locale } = usePreferences();
  const text = (v: string) => tr(locale, v);
  const bench = loadRepoLensBench();
  const factors = headlineFactors(bench);
  const { tuning, holdout } = bench.retrieval;
  const { sonnetHoldout, haikuTuning, haikuHoldout, sonnetScale } = bench.agents;
  const fmtPct = (v: number) => v.toFixed(2);
  const fmtMs = (v: number) => `${Math.round(v).toLocaleString("en-US")} ms`;
  const fmtKb = (v: number) => `${kb(v).toFixed(1)} KB`;
  const fmtN = (v: number) => v.toLocaleString("en-US", { maximumFractionDigits: 2 });

  return (
    <div className="rl-root space-y-8 [--rl-grep:#2a78d6] [--rl-hybrid:#eb6834] [--rl-graph:#1baf7a] dark:[--rl-grep:#3987e5] dark:[--rl-hybrid:#d95926] dark:[--rl-graph:#199e70]">
      <section aria-labelledby="rl-headline">
        <h3 id="rl-headline" className="font-display text-2xl font-semibold text-slate-900 dark:text-slate-100">
          {text("Headline factors from the offline benches")}
        </h3>
        <p className="mt-2 text-sm leading-relaxed text-slate-700 dark:text-slate-300">
          {text("Labels use the same rounding as RepoLens bench/report.py. Raw numerators and denominators come from the committed JSON.")}
        </p>
        <dl className="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {[
            { label: text("Right files in top 10 (tuning)"), value: factors.tuningRecallLabel, note: `n=${tuning.tasks}` },
            { label: text("Right files in top 10 (held-out)"), value: factors.holdoutRecallLabel, note: `n=${holdout.tasks}` },
            { label: text("Latency per query (tuning)"), value: factors.latencyLabel, note: "grep ÷ graph" },
            { label: text("Text returned per query (tuning)"), value: factors.bytesLabel, note: "KB, top 10" },
            { label: text("Sonnet tool calls (held-out)"), value: factors.sonnetToolCallsLabel, note: `${sonnetHoldout.baseline.runs}+${sonnetHoldout.graph.runs} runs` },
            { label: text("Sonnet answer recall (held-out)"), value: factors.sonnetRecallLabel, note: "baseline vs + RepoLens" },
          ].map((card) => (
            <div key={card.label} className="rounded-2xl border border-slate-200 bg-white p-4 dark:border-slate-700 dark:bg-slate-900">
              <dt className="text-xs font-medium uppercase tracking-wider text-slate-600 dark:text-slate-400">{card.label}</dt>
              <dd className="mt-1 font-display text-3xl font-semibold tabular-nums text-slate-900 dark:text-slate-50">{card.value}</dd>
              <dd className="mt-1 font-mono text-[11px] text-slate-500 dark:text-slate-400">{card.note}</dd>
            </div>
          ))}
        </dl>
      </section>

      <section aria-labelledby="rl-retrieval">
        <h3 id="rl-retrieval" className="font-display text-2xl font-semibold text-slate-900 dark:text-slate-100">
          {text("Offline retrieval")}
        </h3>
        <p className="mt-2 text-sm text-slate-700 dark:text-slate-300">
          {text("Task sets")} <span className="font-mono">notaire</span> (n={tuning.tasks}) {text("and")}{" "}
          <span className="font-mono">notaire-holdout</span> (n={holdout.tasks}). {text("Commit")}{" "}
          <span className="font-mono">{tuning.commit.slice(0, 8)}</span>.
        </p>
        <div className="mt-4 grid gap-4 lg:grid-cols-2">
          <GroupBars
            title={text("recall@10 — tuning")}
            note={`${text("Higher is better")}. n=${tuning.tasks}`}
            bars={retrievalBars(tuning.arms, "recall_at_10")}
            format={fmtPct}
          />
          <GroupBars
            title={text("recall@10 — held-out")}
            note={`${text("Higher is better")}. n=${holdout.tasks}`}
            bars={retrievalBars(holdout.arms, "recall_at_10")}
            format={fmtPct}
          />
          <GroupBars
            title={text("Latency (ms) — tuning")}
            note={text("Lower is better")}
            bars={retrievalBars(tuning.arms, "latency_ms")}
            format={fmtMs}
          />
          <GroupBars
            title={text("Latency (ms) — held-out")}
            note={text("Lower is better")}
            bars={retrievalBars(holdout.arms, "latency_ms")}
            format={fmtMs}
          />
          <GroupBars
            title={text("Output bytes — tuning")}
            note={text("Text returned for top 10 files")}
            bars={retrievalBars(tuning.arms, "output_bytes")}
            format={fmtKb}
          />
          <GroupBars
            title={text("Output bytes — held-out")}
            note={text("Text returned for top 10 files")}
            bars={retrievalBars(holdout.arms, "output_bytes")}
            format={fmtKb}
          />
        </div>
      </section>

      <section aria-labelledby="rl-agents">
        <h3 id="rl-agents" className="font-display text-2xl font-semibold text-slate-900 dark:text-slate-100">
          {text("Offline agent benches")}
        </h3>
        <p className="mt-2 text-sm text-slate-700 dark:text-slate-300">
          {text("Baseline is Read/Grep/Glob. The graph arm adds the RepoLens MCP. Sample sizes are the run counts in each JSON.")}
        </p>
        <div className="mt-4 grid gap-4 lg:grid-cols-2">
          <GroupBars
            title={`Sonnet holdout — ${text("tool calls")}`}
            note={`n=${sonnetHoldout.baseline.runs}+${sonnetHoldout.graph.runs}; ${text("same recall")}`}
            bars={agentBars(sonnetHoldout.baseline, sonnetHoldout.graph, "tool_calls")}
            format={fmtN}
          />
          <GroupBars
            title={`Haiku tuning — ${text("tool calls")}`}
            note={`n=${haikuTuning.baseline.runs}+${haikuTuning.graph.runs}`}
            bars={agentBars(haikuTuning.baseline, haikuTuning.graph, "tool_calls")}
            format={fmtN}
          />
          <GroupBars
            title={`Haiku holdout — ${text("tool calls")}`}
            note={`n=${haikuHoldout.baseline.runs}+${haikuHoldout.graph.runs}`}
            bars={agentBars(haikuHoldout.baseline, haikuHoldout.graph, "tool_calls")}
            format={fmtN}
          />
          <GroupBars
            title={`Sonnet scale — ${text("tool calls")}`}
            note={`n=${sonnetScale.baseline.runs}+${sonnetScale.graph.runs}; ${text("tie on tool calls")}`}
            bars={agentBars(sonnetScale.baseline, sonnetScale.graph, "tool_calls")}
            format={fmtN}
          />
        </div>
      </section>

      <section aria-labelledby="rl-factory">
        <h3 id="rl-factory" className="font-display text-2xl font-semibold text-slate-900 dark:text-slate-100">
          {text("Live factory-loop A/B")}
        </h3>
        <p className="mt-2 text-sm text-slate-700 dark:text-slate-300">
          {text("From")} <span className="font-mono">factory-loop-comparison.md</span>
          {bench.factoryLoop.implemented_issues_n != null
            ? `. ${text("Implemented sample n")} = ${bench.factoryLoop.implemented_issues_n}.`
            : "."}
        </p>
        <FactoryLoopTable pairs={bench.factoryLoop.pairs} caveat={bench.factoryLoop.caveat} />
      </section>

      <p className="text-xs text-slate-500 dark:text-slate-400">
        {text("Snapshot generated")} {bench.generated_at} · {bench.method}
      </p>
    </div>
  );
}
