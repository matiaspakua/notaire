/**
 * Delivery outcomes (#1985), read at build time from lib/delivery-metrics.json, which
 * workspace/ci/generate-delivery-metrics.py writes from the GitHub API (and the Pages deploy
 * re-runs before every build). Nothing here computes or defaults a number.
 */
import snapshot from "./delivery-metrics.json";

export type Summary = { n: number; median: number | null; p90: number | null };
export type Unavailable = { available: false; reason: string };

export type DeliveryMetrics = {
  generated_at: string;
  generated_by: string;
  repository: string;
  window: { from: string; to: string; days: number };
  method: Record<"lead_time" | "throughput" | "change_failure" | "restore_main" | "first_push_ci" | "issues_closed" | "releases", string>;
  lead_time_hours: Summary;
  throughput: { merged_prs: number; bot_prs_excluded: number; weeks: number; median_per_week: number | null; series: { week: string; merged: number }[] };
  change_failure: { failure_prs: number; merged_prs: number; rate_pct: number | null; examples: string[] };
  restore_main_hours: Unavailable | ({ available: true; red_episodes: number; main_runs: number; still_red: boolean } & Summary);
  first_push_ci: Unavailable | { available: true; prs_with_ci: number; judged: number; passed: number; rate_pct: number | null };
  issues_closed: number;
  releases: { count: number; tags: { tag: string; published: string }[] };
};

export function loadOutcomes(): DeliveryMetrics {
  return snapshot as DeliveryMetrics;
}

/** "0.5 h", "n/a" for an empty sample: never a made-up zero. */
export function hoursText(h: number | null): string {
  return h === null ? "n/a" : `${h.toLocaleString("en-US", { maximumFractionDigits: 1 })} h`;
}

export function pctText(p: number | null): string {
  return p === null ? "n/a" : `${p.toLocaleString("en-US", { maximumFractionDigits: 1 })}%`;
}
