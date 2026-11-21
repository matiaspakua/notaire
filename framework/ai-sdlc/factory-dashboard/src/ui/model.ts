/** Shapes of the aisdlc serve contract (openapi/factory-control.openapi.yaml) the UI reads. */

export interface Capabilities {
  approvals_live: boolean;
  runner: boolean;
  forge_write: boolean;
  isolator: { sandbox: boolean; enforces_committer: boolean; backend: string | null; status: string | null };
}

export interface Gate {
  stage: string;
  gate: string;
  runs: number;
  pass_rate: number | null;
  median_seconds: number | null;
  last_status: string;
  last_ts: string;
}

export interface Report {
  runs: number;
  failed_runs: number;
  gates: Gate[];
  stages: { stage: string; runs: number; ok_runs: number; median_seconds: number }[];
  cost: {
    total_usd?: number;
    by_model?: Record<string, { entries: number; input: number; output: number; usd: number }>;
  };
  autonomy?: { level: number; status: string; successes: number; trials: number; detail?: string };
}

export interface Step {
  id: string;
  status: string;
}

export interface Run {
  id: string;
  project: string;
  workflow: { id: string; version?: number; digest?: string } | string;
  status: string;
  requested_by?: string;
  requested_at?: string;
  steps?: Step[];
  evidence_head?: string;
}

export interface RunEvent {
  kind: string;
  ts?: string;
  step?: string;
  step_status?: string;
  actor?: string;
}

export interface Repo {
  id: string;
  forge: string;
  url: string;
  default_branch: string;
  mode: "dry_run" | "live";
}

export interface Project {
  id: string;
  profile_ref: string;
  repos: Repo[];
}

export interface Entry {
  id: string;
  kind: string;
  ref: string | null;
  summary: string;
  version: number;
  digest?: string;
  created_by?: string;
  created_at?: string;
  governance?: string;
  [field: string]: unknown;
}

export interface Workflow {
  id: string;
  version?: number;
  governance?: string;
  summary?: string;
}

export interface CoachReport {
  scope: { project_only: boolean; days: number | null };
  totals: { sessions: number; healthy_sessions: number; user_turns: number; tool_calls: number };
  harnesses: {
    harness: string;
    sessions: number;
    user_turns: number;
    tool_calls: number | null;
    models: Record<string, number>;
    tools: Record<string, number>;
    first: string | null;
    last: string | null;
  }[];
  anti_patterns: { id: string; severity: "high" | "medium" | "low"; occurrences: number; sessions: number; recommendation: string }[];
  hours: number[];
}

export interface Hit {
  id: string;
  kind: string;
  name: string;
  path: string;
  start: number;
  project?: string;
  why?: string;
  score?: number;
}

export interface KnowledgeMetrics {
  index: Record<string, unknown>;
  orphans: { isolated: number; history_only: number; demoted: number; conventional: number };
  usage: {
    server?: { calls?: number; p95_ms?: number; zero_result_rate?: number };
    sessions?: { sessions?: number; adoption?: number };
  };
}

export const ENVELOPE = new Set(["id", "kind", "ref", "summary", "version", "digest", "created_by", "created_at",
  "schema_version", "governance", "source"]);
export const SECRET_FIELD = /(token|secret|password|passwd|api_key|authorization|credential)/i;
