import snap from "./factory-metrics-snapshot.json";

/** Time, tokens, money and RepoLens per intent and overall, from `aisdlc metrics factory` (#2447). */
export type FactoryIntent = {
  title: string;
  runs: number;
  active_s: number;
  wall_s: number | null;
  corrections: number;
  tokens: number;
  usd: number | null;
  cost_note: "none" | "partial" | "reported" | "priced";
  repolens_calls: number;
};

export type FactoryBench = {
  model: string | null;
  file: string;
  runs_per_arm: number;
  cost_change_pct: number | null;
  input_tokens_change_pct: number | null;
  tool_calls_change_pct: number | null;
  time_change_pct: number | null;
  recall_change: number | null;
};

export type FactoryMetricsSnapshot = {
  generated_at: string;
  method: string;
  overall: {
    intents: number;
    runs: number;
    succeeded: number;
    corrections: number;
    median_active_s: number | null;
    median_wall_s: number | null;
    median_usd: number | null;
    median_tokens: number | null;
    usd: { total: number | null; attributed: number | null; overhead: number | null };
    tokens: { input: number; output: number; cache_read: number; cache_write: number; total: number };
    repolens_calls: number;
    repolens_p50_ms: number | null;
  };
  intents: FactoryIntent[];
  repolens: { bench: FactoryBench[]; live_pairs: { with_repolens: number; without: number } };
};

export const FACTORY_METRICS = snap as FactoryMetricsSnapshot;

export function usd(value: number | null): string {
  return value === null ? "n/a" : `$${value.toFixed(2)}`;
}

export function minutes(seconds: number | null): string {
  return seconds === null ? "n/a" : `${(seconds / 60).toFixed(1)} min`;
}

export function pct(value: number | null): string {
  if (value === null) return "n/a";
  return `${value < 0 ? "−" : "+"}${Math.abs(value).toFixed(1)}%`;
}

export function millions(value: number): string {
  return `${(value / 1e6).toFixed(1)}M`;
}
