/**
 * Factory Dashboard — Core types.
 *
 * Mirrors the AI SDLC OpenAPI entity model (runs, workflows, evidence events)
 * and the production line station/agent model.
 */

// ───────────────────── Evidence events ─────────────────────

export type EvidenceKind =
  | "run.request"
  | "run.started"
  | "run.finished"
  | "run.paused"
  | "run.resumed"
  | "run.stopped"
  | "step.started"
  | "step.finished"
  | "step.skipped"
  | "stage.run"
  | "cost";

export interface EvidenceEvent {
  kind: EvidenceKind;
  run_id?: string;
  step?: string;
  stage?: string;
  status?: string;
  timestamp: string;
  [key: string]: unknown;
}

// ───────────────────── Run model ─────────────────────

export type RunStatus =
  | "requested"
  | "queued"
  | "running"
  | "paused"
  | "succeeded"
  | "failed"
  | "cancelled"
  | "blocked";

export interface RunStep {
  id: string;
  status: RunStatus;
  agent?: string;
}

export interface Run {
  id: string;
  status: RunStatus;
  workflow: { id: string; version: number };
  requested_by: string;
  requested_at: string;
  steps: RunStep[];
  events: EvidenceEvent[];
}

// ───────────────────── Workflow model ─────────────────────

export type WorkflowStatus = "draft" | "validated" | "approved" | "archived";

export interface WorkflowStep {
  id: string;
  stage: string;
  agent?: string;
  skills: string[];
  mcp: string[];
  gates: string[];
}

export interface Workflow {
  id: string;
  version: number;
  status: WorkflowStatus;
  steps: WorkflowStep[];
  created_at: string;
}

// ───────────────────── Production line ─────────────────────

export type StationKind = "intake" | "plan" | "prove" | "publish" | "ship" | "gate" | "human";

export type StationPhase = "Intake" | "Plan" | "Prove" | "Publish" | "Ship";

export interface StationGate {
  number: number;
  name: string;
  check: string;
}

export interface StationHuman {
  role: string;
  action: string;
}

export interface Station {
  id: string;
  kind: StationKind;
  title: string;
  phase: StationPhase;
  agent?: string;
  status?: "idle" | "active" | "completed" | "blocked" | "waiting" | "skipped" | "paused";
  gate?: StationGate;
  human?: StationHuman;
  skills: string[];
  tools: string[];
}

// ───────────────────── Agent model ─────────────────────

export interface Agent {
  name: string;
  emoji: string;
  role: string;
  status: "idle" | "working" | "offline";
  current_station?: string;
}

// ───────────────────── Configuration: Agents ─────────────────────

export type AgentProvider = "claude" | "cursor" | "codex" | "agy" | "kiro" | "opencode" | "custom";

export interface AgentConfig {
  id: string;
  name: string;
  provider: AgentProvider;
  role: string;
  description: string;
  enabled: boolean;
  model?: string;
  max_tokens?: number;
  temperature?: number;
  system_prompt?: string;
  allowed_tools?: string[];
  associated_stations?: string[];
  created_at: string;
  updated_at: string;
}

// ───────────────────── Configuration: Rules ─────────────────────

export type RuleCategory = "workflow" | "code-quality" | "security" | "architecture" | "migration" | "ui-ux" | "general";

export interface RuleConfig {
  id: string;
  name: string;
  category: RuleCategory;
  description: string;
  enabled: boolean;
  file_path: string;
  severity: "error" | "warning" | "info";
  associated_agents?: string[];
  associated_stations?: string[];
  created_at: string;
  updated_at: string;
}

// ───────────────────── Configuration: MCP Integrations ─────────────────────

export type McpTransport = "stdio" | "http" | "websocket";

export interface McpServerConfig {
  id: string;
  name: string;
  description: string;
  enabled: boolean;
  transport: McpTransport;
  command?: string;
  args?: string[];
  url?: string;
  env?: Record<string, string>;
  tools?: string[];
  associated_agents?: string[];
  associated_stations?: string[];
  health_status?: "healthy" | "unhealthy" | "unknown";
  last_health_check?: string;
  created_at: string;
  updated_at: string;
}

// ───────────────────── Configuration: Skills ─────────────────────

export interface SkillConfig {
  id: string;
  name: string;
  description: string;
  enabled: boolean;
  file_path: string;
  category?: string;
  tags?: string[];
  associated_agents?: string[];
  associated_stations?: string[];
  is_generic: boolean;
  created_at: string;
  updated_at: string;
}

// ───────────────────── Configuration: Associations ─────────────────────

export interface StationAssociation {
  station_id: string;
  agents: string[];
  rules: string[];
  mcp_servers: string[];
  skills: string[];
}

// ───────────────────── Configuration: Collection responses ─────────────────────

export interface ConfigCollection<T> {
  items: T[];
  total: number;
}

// ───────────────────── API base (needed by CONFIG_ENDPOINTS below) ─────────────────────

const API_BASE = "/api/factory";

// ───────────────────── Configuration: API endpoints ─────────────────────

export const CONFIG_ENDPOINTS = {
  // Agents
  agents: `${API_BASE}/config/agents`,
  agentById: (id: string) => `${API_BASE}/config/agents/${id}`,
  // Rules
  rules: `${API_BASE}/config/rules`,
  ruleById: (id: string) => `${API_BASE}/config/rules/${id}`,
  // MCP
  mcpServers: `${API_BASE}/config/mcp`,
  mcpServerById: (id: string) => `${API_BASE}/config/mcp/${id}`,
  mcpHealth: (id: string) => `${API_BASE}/config/mcp/${id}/health`,
  // Skills
  skills: `${API_BASE}/config/skills`,
  skillById: (id: string) => `${API_BASE}/config/skills/${id}`,
  // Associations
  stationAssociations: `${API_BASE}/config/associations`,
  stationAssociationById: (stationId: string) => `${API_BASE}/config/associations/${stationId}`,
} as const;

// ───────────────────── Intent (factory input) ─────────────────────

export interface Intent {
  id: string;
  source: "github" | "jira" | "cli" | "mcp";
  title: string;
  description: string;
  created_at: string;
  run_id?: string;
}

// ───────────────────── Dashboard state ─────────────────────

export interface DashboardState {
  intent: Intent | null;
  run: Run | null;
  stations: Station[];
  agents: Agent[];
  events: EvidenceEvent[];
  capabilities: Capabilities;
}

// ───────────────────── Capabilities ─────────────────────

export interface Capabilities {
  approvals_live: boolean;
  runner: boolean;
  forge_write: boolean;
  isolator: {
    sandbox: boolean;
    enforces_committer: boolean;
  };
}

// ───────────────────── WebSocket messages ─────────────────────

export type WsMessageType =
  | "event"
  | "run_status"
  | "station_update"
  | "agent_update"
  | "intent_update"
  | "heartbeat"
  | "control_ack"
  | "control_error";

export interface WsMessage {
  type: WsMessageType;
  timestamp: string;
  data: unknown;
}

// ───────────────────── Control commands (bidirectional) ─────────────────────

export type ControlCommand = "play" | "pause" | "resume" | "stop" | "skip";

export interface ControlRequest {
  command: ControlCommand;
  run_id?: string;
  step_id?: string;
  reason?: string;
}

export interface ControlResponse {
  command: ControlCommand;
  accepted: boolean;
  run_id?: string;
  message?: string;
  timestamp: string;
}

export type ControlState = "idle" | "playing" | "paused" | "stopped";

// ───────────────────── API endpoints ─────────────────────

export const ENDPOINTS = {
  capabilities: `${API_BASE}/capabilities`,
  runs: `${API_BASE}/runs`,
  runsById: (id: string) => `${API_BASE}/runs/${id}`,
  runEvents: (id: string) => `${API_BASE}/runs/${id}/events`,
  runControl: (id: string) => `${API_BASE}/runs/${id}/control`,
  workflows: `${API_BASE}/workflows`,
  config: `${API_BASE}/config`,
  report: `${API_BASE}/report`,
  metrics: `${API_BASE}/metrics`,
} as const;

// ───────────────────── Metrics ─────────────────────

export interface StageDuration {
  stage: string;
  runs: number;
  okRuns: number;
  medianSeconds: number;
  totalSeconds: number;
}

export interface TokenUsage {
  timestamp: string;
  inputTokens: number;
  outputTokens: number;
  totalTokens: number;
  costUsd: number;
  stage: string;
}

export interface ErrorBucket {
  category: string;
  count: number;
}

export interface RunLeadTime {
  issue: number;
  pullRequest: number;
  seconds: number;
  startAt: string;
  endAt: string;
}

export interface MetricsSnapshot {
  // Summary KPIs
  totalRuns: number;
  successfulRuns: number;
  failedRuns: number;
  avgLeadTimeSeconds: number;
  medianLeadTimeSeconds: number;
  totalTokenCost: number;
  totalTokens: number;
  totalInputTokens: number;
  totalOutputTokens: number;
  errorRate: number;
  avgTokensPerRun: number;

  // Time series
  stageDurations: StageDuration[];
  tokenUsage: TokenUsage[];
  errorBuckets: ErrorBucket[];
  leadTimes: RunLeadTime[];

  // Throughput
  runsByDay: { date: string; count: number }[];
}
