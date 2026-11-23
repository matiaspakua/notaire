import { Card, SectionTitle, Sources, Table } from "@/components/FactoryUI";
import { T } from "@/components/site/T";
import { src } from "@/lib/ai-sdlc-content";
import { FACTORY_METRICS as M, millions, minutes, pct, usd } from "@/lib/factory-metrics";

const NOTE: Record<string, string> = {
  none: "no cost recorded",
  partial: "some rows unpriced",
  reported: "as reported by the worker",
  priced: "",
};

/** The factory's own answers: time, money and RepoLens per intent and overall (#2447). */
export function FactoryMetricsSection() {
  const o = M.overall;
  const overheadShare = o.usd.total && o.usd.overhead ? Math.round((100 * o.usd.overhead) / o.usd.total) : null;
  const largest = M.repolens.bench.reduce<(typeof M.repolens.bench)[number] | null>(
    (a, b) => (a === null || b.runs_per_arm > a.runs_per_arm ? b : a),
    null,
  );
  const kpis = [
    { label: "Time per intent", value: minutes(o.median_active_s), note: `median active time · ${minutes(o.median_wall_s)} wall` },
    { label: "Cost per intent", value: usd(o.median_usd), note: "median, Claude tokens at list price" },
    {
      label: "Total cost so far",
      value: usd(o.usd.total),
      note: overheadShare === null ? `${o.runs} runs` : `${overheadShare}% of it spent between runs`,
    },
    { label: "Tokens", value: millions(o.tokens.total), note: `${millions(o.tokens.cache_read)} of them cache reads` },
  ];
  return (
    <section aria-labelledby="factory-metrics">
      <SectionTitle id="factory-metrics">What each intent cost</SectionTitle>
      <p className="leading-relaxed text-slate-700">
        <T text="Every factory run records its steps in the evidence ledger. The tokens come from the worker logs (Junie) and the Foreman's session (Claude Code), attributed to each run by time window and priced at list price. Work between runs is counted as overhead, not hidden." />
      </p>
      <dl className="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
        {kpis.map((k) => (
          <Card key={k.label}>
            <dt className="text-xs font-medium uppercase tracking-wider text-slate-600">
              <T text={k.label} />
            </dt>
            <dd className="mt-1 font-display text-4xl font-semibold tabular-nums text-slate-900">{k.value}</dd>
            <dd className="mt-1 text-xs text-slate-600">{k.note}</dd>
          </Card>
        ))}
      </dl>
      <div className="mt-6">
        <Table
          head={["Intent", "Active", "Wall", "Corrections", "Tokens", "Cost", "RepoLens calls"]}
          rows={M.intents.map((i) => [
            i.title,
            minutes(i.active_s),
            minutes(i.wall_s),
            String(i.corrections),
            millions(i.tokens),
            <span key="cost">
              {usd(i.usd)}
              {NOTE[i.cost_note] ? <span className="block text-xs text-slate-500">{NOTE[i.cost_note]}</span> : null}
            </span>,
            String(i.repolens_calls),
          ])}
        />
      </div>
      <h3 className="mt-10 font-display text-xl font-semibold text-slate-900">
        <T text="What RepoLens changes" />
      </h3>
      <p className="mt-2 leading-relaxed text-slate-700">
        <T text="Same model and tasks, with and without RepoLens. Negative cost, token, tool-call and time changes are savings; recall is the share of the files an engineer would edit that the agent found." />
        {largest
          ? ` Largest sample: ${largest.model} (${largest.runs_per_arm} runs per arm), cost ${pct(largest.cost_change_pct)}, time ${pct(largest.time_change_pct)}.`
          : ""}
      </p>
      <div className="mt-4">
        <Table
          head={["Benchmark", "Runs per arm", "Cost", "Input tokens", "Tool calls", "Time", "Recall"]}
          rows={M.repolens.bench.map((b) => [
            b.model ?? b.file,
            String(b.runs_per_arm),
            pct(b.cost_change_pct),
            pct(b.input_tokens_change_pct),
            pct(b.tool_calls_change_pct),
            pct(b.time_change_pct),
            b.recall_change === null ? "n/a" : `${b.recall_change >= 0 ? "+" : "−"}${Math.abs(b.recall_change * 100).toFixed(1)} pt`,
          ])}
        />
      </div>
      <ul className="mt-4 list-disc space-y-2 pl-5 text-sm text-slate-700">
        <li>
          Junie runs on a plan that reports no per-token charge, so its tokens are counted but cost $0; the money is the
          Foreman&apos;s Claude usage.
        </li>
        <li>RepoLens helps the smaller model most in these benchmarks; with Sonnet it saves tool calls but not money.</li>
        <li>
          Paired factory runs (the same kind of intent with and without RepoLens): {M.repolens.live_pairs.with_repolens} with,{" "}
          {M.repolens.live_pairs.without} without. Until both reach two, the factory-level effect is not measured.
        </li>
        <li>
          Generated {M.generated_at.slice(0, 10)} by <code className="font-mono">{M.method}</code>.
        </li>
      </ul>
      <Sources
        items={[
          { label: "factory_metrics.py", href: src("framework/ai-sdlc/aisdlc/factory_metrics.py") },
          { label: "usage_import.py", href: src("framework/ai-sdlc/aisdlc/usage_import.py") },
          { label: "factory-metrics-snapshot.json", href: src("github-page/lib/factory-metrics-snapshot.json") },
          { label: "bench results", href: src("framework/repolens/bench/results") },
        ]}
      />
    </section>
  );
}
