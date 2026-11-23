export interface TokenCounts {
  input: number;
  output: number;
  cache_read: number;
  cache_write: number;
}

export interface CostSummary {
  usd: number | null;
  fully_priced: boolean;
  rows: number;
  reported_rows: number;
}

export interface TimeSummary {
  wall_s: number | null;
  active_s: number;
  by_stage: Record<string, number>;
  corrections: number;
}

export interface RepolensUsage {
  calls: number;
  tokens_served: number;
  p50_ms: number | null;
  zero_result_rate: number | null;
}

export interface IntentMetric {
  id: string;
  title: string | null;
  runs: string[];
  time: TimeSummary;
  tokens: TokenCounts;
  cost: CostSummary;
  repolens: RepolensUsage;
}

export interface BenchEffect {
  file: string;
  model: string | null;
  runs_per_arm: number;
  cost_usd_change_pct: number | null;
  input_tokens_change_pct: number | null;
  tool_calls_change_pct: number | null;
  duration_change_pct: number | null;
  recall_change: number | null;
}

export interface FactoryMetrics {
  generated_at: string;
  intents: IntentMetric[];
  overall: {
    runs: number;
    intents: number;
    succeeded: number;
    time: { active_s: number; corrections: number };
    tokens: TokenCounts;
    cost: CostSummary;
    attributed?: { cost: CostSummary };
    overhead?: { cost: CostSummary };
    repolens: RepolensUsage;
    per_intent: { median_wall_s: number | null; median_active_s: number | null; median_usd: number | null; median_tokens: number | null };
  };
  repolens_effect: { bench: BenchEffect[]; live_ab: { metrics?: Record<string, { a: { n: number }; b: { n: number } }> } };
}

export type CostNote = "none" | "partial" | "reported" | "priced";

export function costLabel(cost: CostSummary, lang: string): { text: string; note: CostNote } {
  if (cost.usd === null) return { text: "—", note: "none" };
  const text = cost.usd.toLocaleString(lang, { style: "currency", currency: "USD", minimumFractionDigits: 2 });
  if (!cost.fully_priced) return { text, note: "partial" };
  return { text, note: cost.reported_rows === cost.rows ? "reported" : "priced" };
}

export function totalTokens(tokens: TokenCounts): number {
  return tokens.input + tokens.output + tokens.cache_read + tokens.cache_write;
}

export function minutes(seconds: number | null, lang: string): string {
  if (seconds === null) return "—";
  return `${(seconds / 60).toLocaleString(lang, { maximumFractionDigits: 1, minimumFractionDigits: 1 })} min`;
}

export function changeLabel(pct: number | null): string {
  if (pct === null) return "—";
  return `${pct < 0 ? "−" : "+"}${Math.abs(pct).toFixed(1)}%`;
}

export function liveSamples(effect: FactoryMetrics["repolens_effect"]): { withRepolens: number; without: number } {
  const metric = Object.values(effect.live_ab.metrics ?? {})[0];
  return { withRepolens: metric?.a.n ?? 0, without: metric?.b.n ?? 0 };
}
