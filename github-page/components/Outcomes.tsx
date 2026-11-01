import type { ReactNode } from "react";
import { type DeliveryMetrics, hoursText, pctText } from "@/lib/outcomes";

type Metric = { id: string; label: string; value: ReactNode; detail: ReactNode; method: string; reason?: string };

function metrics(d: DeliveryMetrics): Metric[] {
  const lead = d.lead_time_hours;
  const restore = d.restore_main_hours;
  const ci = d.first_push_ci;
  return [
    {
      id: "lead",
      label: "PR lead time",
      value: `${hoursText(lead.median)} median`,
      detail: `p90 ${hoursText(lead.p90)} · ${lead.n.toLocaleString("en-US")} merged PRs`,
      method: d.method.lead_time,
    },
    {
      id: "throughput",
      label: "Throughput",
      value: `${d.throughput.median_per_week ?? "n/a"} PRs / week`,
      detail: `median over ${d.throughput.weeks} weeks · ${d.throughput.merged_prs.toLocaleString("en-US")} merged, ${d.throughput.bot_prs_excluded} bot PRs excluded`,
      method: d.method.throughput,
    },
    {
      id: "cfr",
      label: "Change-failure proxy",
      value: pctText(d.change_failure.rate_pct),
      detail: `${d.change_failure.failure_prs} revert or hotfix PRs of ${d.change_failure.merged_prs.toLocaleString("en-US")}${d.change_failure.examples.length ? ` (${d.change_failure.examples.join(", ")})` : ""}`,
      method: d.method.change_failure,
    },
    restore.available
      ? {
          id: "restore",
          label: "Time to restore main",
          value: `${hoursText(restore.median)} median`,
          detail: `p90 ${hoursText(restore.p90)} · ${restore.red_episodes} red episodes in ${restore.main_runs} runs`,
          method: d.method.restore_main,
        }
      : { id: "restore", label: "Time to restore main", value: "not available", detail: null, method: d.method.restore_main, reason: restore.reason },
    ci.available
      ? {
          id: "first-push",
          label: "First-push CI pass rate",
          value: pctText(ci.rate_pct),
          detail: `${ci.passed} of ${ci.judged} PRs passed CI on their first run`,
          method: d.method.first_push_ci,
        }
      : { id: "first-push", label: "First-push CI pass rate", value: "not available", detail: null, method: d.method.first_push_ci, reason: ci.reason },
    {
      id: "issues",
      label: "Issues closed",
      value: d.issues_closed.toLocaleString("en-US"),
      detail: "closed as completed",
      method: d.method.issues_closed,
    },
    {
      id: "releases",
      label: "Releases",
      value: String(d.releases.count),
      detail: d.releases.tags.length ? `latest ${d.releases.tags[d.releases.tags.length - 1].tag}` : "none in the window",
      method: d.method.releases,
    },
  ];
}

/**
 * Delivery outcomes (#1985): each value from the snapshot, next to how it was computed and the
 * window it covers. A metric the GitHub data cannot support says so and why.
 */
export function OutcomesSection({ data }: { data: DeliveryMetrics }) {
  const list = metrics(data);
  return (
    <section aria-labelledby="outcomes">
      <h2 id="outcomes" className="mb-4 mt-16 font-display text-2xl font-semibold tracking-tight text-slate-900 text-balance sm:text-3xl">
        Delivery outcomes
      </h2>
      <p className="max-w-[65ch] leading-relaxed text-slate-700">
        From the GitHub API for <span className="font-semibold">{data.window.from}</span> to{" "}
        <span className="font-semibold">{data.window.to}</span> ({data.window.days} days), generated{" "}
        {data.generated_at.slice(0, 10)} by <code className="font-mono text-[13px]">{data.generated_by}</code>.
      </p>
      <div className="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
        {list.map((m) => (
          <div key={m.id} role="group" aria-labelledby={`outcome-${m.id}`} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-[var(--shadow-card)]">
            <p id={`outcome-${m.id}`} className="text-xs font-medium uppercase tracking-wider text-slate-600">{m.label}</p>
            <p className={`mt-1 font-display font-semibold tabular-nums ${m.reason ? "text-xl text-slate-600" : "text-3xl text-slate-900"}`}>{m.value}</p>
            {m.detail && <p className="mt-1 text-sm text-slate-700">{m.detail}</p>}
            {m.reason && <p className="mt-1 text-sm text-slate-700">{m.reason}</p>}
            <details className="mt-3 text-xs text-slate-600">
              <summary className="cursor-pointer font-medium text-slate-700">How it is computed</summary>
              <p className="mt-1 leading-relaxed">{m.method}</p>
            </details>
          </div>
        ))}
      </div>
    </section>
  );
}
