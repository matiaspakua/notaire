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

export interface StepIoLink {
  label?: string;
  url: string;
}

export interface StepIo {
  request?: string;
  prompt?: string;
  summary?: string;
  files?: string[];
  commits?: string[];
  links?: StepIoLink[];
}

export interface RunStep extends Step {
  stage?: string | null;
  inputs?: StepIo;
  outputs?: StepIo;
}

export interface Run {
  id: string;
  project: string;
  workflow: { id: string; version?: number; digest?: string } | string;
  status: string;
  requested_by?: string;
  requested_at?: string;
  steps?: RunStep[];
  evidence_head?: string;
  intent?: string;
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

export interface LineStageGuardrail {
  kind: "grant";
  id: string;
  stage: string;
  worktree?: string;
  write_set?: string[];
  required_checks?: string[];
  budget?: Record<string, unknown>;
}

export interface LineStage {
  id: string;
  gates: LineGate[];
  inputs: string[];
  outputs: string[];
  io_source?: string;
  agents: string[];
  skills: string[];
  mcp: string[];
  tools: string[];
  model?: string | null;
  effort: Record<string, number>;
  guardrails: LineStageGuardrail[];
  kind?: "agent" | "deterministic" | "human";
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


export interface ConfigFileEntry {
  path: string;
  kind: string;
  size: number;
  media_type: string;
  read_only: boolean;
}

export interface ConfigFileContent {
  path: string;
  content: string;
  redacted: boolean;
  media_type: string;
  size: number;
}

export interface TelemetryRow {
  key: string;
  runs: number;
  duration_s: number;
  usd: number;
  failures: number;
}

export interface TelemetryResponse {
  group: string;
  rows: TelemetryRow[];
  total_duration_s: number;
  total_usd: number;
  total_failures: number;
  total_runs: number;
  failures_by_integration: Record<string, number>;
  repolens: {
    calls?: number;
    avg_ms?: number;
    total_ms?: number;
    by_tool?: Record<string, { calls: number; avg_ms: number; total_ms: number }>;
  };
}

export interface ConfigTreeNode {
  name: string;
  path?: string;
  kind?: string;
  children: ConfigTreeNode[];
}

export function buildConfigTree(entries: ConfigFileEntry[]): ConfigTreeNode[] {
  const root: ConfigTreeNode = { name: "", children: [] };
  for (const entry of entries) {
    const parts = entry.path.split("/");
    let node = root;
    for (let i = 0; i < parts.length; i++) {
      const part = parts[i];
      if (i === parts.length - 1) {
        const existing = node.children.find((c) => c.name === part && c.path);
        if (!existing) {
          node.children.push({ name: part, path: entry.path, kind: entry.kind, children: [] });
        }
      } else {
        let dir = node.children.find((c) => c.name === part && !c.path);
        if (!dir) {
          dir = { name: part, children: [] };
          node.children.push(dir);
        }
        node = dir;
      }
    }
  }
  const sortChildren = (nodes: ConfigTreeNode[]) => {
    nodes.sort((a, b) => {
      const aIsFile = Boolean(a.path);
      const bIsFile = Boolean(b.path);
      if (aIsFile !== bIsFile) return aIsFile ? -1 : 1;
      return a.name.localeCompare(b.name);
    });
    for (const node of nodes) {
      if (node.children.length) sortChildren(node.children);
    }
  };
  sortChildren(root.children);
  return root.children;
}

export function formatDuration(seconds: number, lang: string): string {
  if (seconds >= 3600) {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    return m > 0 ? `${h}h ${m}m` : `${h}h`;
  }
  if (seconds >= 60) {
    const m = Math.floor(seconds / 60);
    const s = Math.floor(seconds % 60);
    return s > 0 ? `${m}m ${s}s` : `${m}m`;
  }
  return `${seconds.toLocaleString(lang, { maximumFractionDigits: seconds < 1 ? 2 : 1 })}s`;
}

export function encodeConfigPath(path: string): string {
  return path.split("/").map(encodeURIComponent).join("/");
}

export function isSafeLink(url: string): boolean {
  try {
    const u = new URL(url);
    return u.protocol === "http:" || u.protocol === "https:";
  } catch {
    return false;
  }
}

/** The intent the bar shows: the current run's, else the most recently submitted one (#2447). */
export function pickIntent(intentId: string | null | undefined, entries: Intent[]): Intent | null {
  if (intentId) return entries.find((i) => i.id === intentId) ?? null;
  return [...entries].sort((x, y) => (y.created_at ?? "").localeCompare(x.created_at ?? ""))[0] ?? null;
}
