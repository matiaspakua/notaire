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

export interface LineGate {
  id: string;
  name: string;
  command?: string;
  required: boolean;
  mode: "required" | "advisory";
  expect?: string;
  paths?: string[];
}

export interface LineStage {
  id: string;
  gates: LineGate[];
  inputs: string[];
  outputs: string[];
  io_source?: string;
}

export interface LineGuardrails {
  protected: { owner?: string; paths: string[] };
  hooks: string[];
  rules: unknown[];
  isolator: { status?: string; sandbox: boolean; backend?: string | null; enforces_committer: boolean };
}

export interface LinePayload {
  schema_version?: number;
  stages: LineStage[];
  guardrails: LineGuardrails;
}

export interface ConfigStage {
  id: string;
  agents: string[];
  skills: string[];
  mcp: string[];
  model?: string | null;
  effort: Record<string, number>;
  inputs: string[];
  outputs: string[];
}

export interface ConfigPayload {
  stages: ConfigStage[];
}

export interface StatusPrevious {
  step: string;
  stage?: string | null;
  status: string;
  started_at?: string | null;
  finished_at?: string | null;
  duration_s?: number | null;
  usd?: number | null;
}

export interface StatusNext {
  step: string;
  stage?: string | null;
  source?: string;
}

export interface FactoryStatus {
  intent: string | null;
  run: string | null;
  workflow?: { id: string; version?: number; digest?: string } | string | null;
  run_status: string | null;
  stage: string | null;
  step: string | null;
  previous: StatusPrevious[];
  next: StatusNext[];
  updated_at: string | null;
}

export interface IntentRef {
  label: string;
  url: string;
  kind?: string;
}

export interface Intent {
  id: string;
  kind: "prompt" | "reference" | "spec";
  title: string;
  text: string;
  source: { system: string; ref: string };
  acceptance: string[];
  references: IntentRef[];
  status: string;
  created_by?: string;
  created_at?: string;
  run_ids?: string[];
  version?: number;
}

export interface MergedStation {
  id: string;
  gates: LineGate[];
  inputs: string[];
  outputs: string[];
  agents: string[];
  skills: string[];
  mcp: string[];
  model?: string | null;
  effort: Record<string, number>;
  guardrails: LineGuardrails;
  state: "done" | "current" | "upcoming";
}

/** Merge line stages with config stage details by id. Tolerates missing keys. */
export function mergeLineConfig(line: LinePayload, config: ConfigPayload | undefined): LineStage[] {
  const configMap = new Map<string, ConfigStage>();
  config?.stages.forEach((s) => configMap.set(s.id, s));
  return line.stages.map((stage) => {
    const cfg = configMap.get(stage.id);
    return {
      ...stage,
      agents: cfg?.agents ?? [],
      skills: cfg?.skills ?? [],
      mcp: cfg?.mcp ?? [],
      model: cfg?.model ?? null,
      effort: cfg?.effort ?? {},
    } as LineStage & { agents: string[]; skills: string[]; mcp: string[]; model?: string | null; effort: Record<string, number> };
  });
}

export function stationState(stationId: string, status: FactoryStatus | undefined): "done" | "current" | "upcoming" {
  if (!status || !status.run_status) return "upcoming";
  if (status.stage === stationId && status.run_status === "running") return "current";
  const prevDone = status.previous.some((p) => p.stage === stationId);
  if (prevDone) return "done";
  return "upcoming";
}

/** Return only http/https URLs as safe links; everything else becomes plain text. */
export function isSafeLink(url: string): boolean {
  try {
    const u = new URL(url);
    return u.protocol === "http:" || u.protocol === "https:";
  } catch {
    return false;
  }
}
