import type { FactoryStatus, LineStage, TelemetryRow } from "./model.ts";

export type StationKind = "agent" | "human" | "deterministic";
export type StationState = "done" | "running" | "failed" | "next" | "idle";
export type GateState = "passed" | "failed" | "not-evaluable" | "not-run";

export interface WorkflowStep {
  id: string;
  stage?: string | null;
}

export interface Throughput {
  runs: number;
  avgSeconds: number;
  usd: number;
  failures: number;
}

export interface IntentProgress {
  done: number;
  total: number;
  currentStageId: string | null;
  elapsedSeconds: number | null;
  usd: number | null;
}

// Older kits do not send a kind (#2411); merge is where the Foreman or the Owner decides.
const HUMAN_STAGES = new Set(["merge"]);
const ACTIVE_RUN = new Set(["running", "queued", "requested", "waiting"]);

export function normalizeStage(stage: Partial<LineStage> & { id: string }): LineStage {
  return {
    id: stage.id,
    gates: stage.gates ?? [],
    inputs: stage.inputs ?? [],
    outputs: stage.outputs ?? [],
    io_source: stage.io_source,
    agents: stage.agents ?? [],
    skills: stage.skills ?? [],
    mcp: stage.mcp ?? [],
    tools: stage.tools ?? [],
    model: stage.model ?? null,
    effort: stage.effort ?? {},
    guardrails: stage.guardrails ?? [],
    kind: stage.kind,
  };
}

export function stationKind(stage: LineStage): StationKind {
  if (stage.kind) return stage.kind;
  if (stage.agents.length > 0) return "agent";
  if (HUMAN_STAGES.has(stage.id)) return "human";
  return "deterministic";
}

export function stageOfStep(step: string | null | undefined, steps: WorkflowStep[], stages: LineStage[]): string | null {
  if (!step) return null;
  const mapped = steps.find((s) => s.id === step)?.stage;
  if (mapped && stages.some((s) => s.id === mapped)) return mapped;
  return stages.some((s) => s.id === step) ? step : null;
}

function stageIndex(stages: LineStage[], id: string | null): number {
  return id ? stages.findIndex((s) => s.id === id) : -1;
}

function furthestReached(stages: LineStage[], status: FactoryStatus, steps: WorkflowStep[]): number {
  return status.previous.reduce((max, p) => {
    const index = stageIndex(stages, p.stage ?? stageOfStep(p.step, steps, stages));
    return Math.max(max, index);
  }, -1);
}

export function currentStage(stages: LineStage[], status: FactoryStatus | undefined, steps: WorkflowStep[]): string | null {
  if (!status) return null;
  if (status.stage && stages.some((s) => s.id === status.stage)) return status.stage;
  return stageOfStep(status.step, steps, stages);
}

export function stationStates(
  stages: LineStage[],
  status: FactoryStatus | undefined,
  steps: WorkflowStep[],
): Record<string, StationState> {
  const states: Record<string, StationState> = Object.fromEntries(stages.map((s) => [s.id, "idle" as StationState]));
  if (!status?.run || !status.run_status) return states;
  const failed = status.run_status === "failed";
  const current = stageIndex(stages, currentStage(stages, status, steps));
  const reached = furthestReached(stages, status, steps);
  if (ACTIVE_RUN.has(status.run_status) || (failed && current >= 0)) {
    stages.forEach((s, i) => {
      if (current < 0) states[s.id] = i <= reached ? "done" : "next";
      else if (i < current) states[s.id] = "done";
      else if (i === current) states[s.id] = failed ? "failed" : "running";
      else states[s.id] = "next";
    });
    return states;
  }
  stages.forEach((s, i) => {
    if (i <= reached) states[s.id] = failed && i === reached ? "failed" : "done";
  });
  return states;
}

export function gateState(lastStatus: string | undefined | null): GateState {
  if (lastStatus === "pass") return "passed";
  if (lastStatus === "fail") return "failed";
  if (lastStatus === "skip" || lastStatus === "todo") return "not-evaluable";
  return "not-run";
}

export function stationThroughput(
  stageId: string,
  rows: TelemetryRow[],
  steps: WorkflowStep[],
  stages: LineStage[],
): Throughput | null {
  const mine = rows.filter((r) => r.runs > 0 && stageOfStep(r.key, steps, stages) === stageId);
  if (mine.length === 0) return null;
  const runs = mine.reduce((n, r) => n + r.runs, 0);
  const seconds = mine.reduce((n, r) => n + r.duration_s, 0);
  return {
    runs,
    avgSeconds: runs > 0 ? seconds / runs : 0,
    usd: mine.reduce((n, r) => n + r.usd, 0),
    failures: mine.reduce((n, r) => n + r.failures, 0),
  };
}

export function intentProgress(
  stages: LineStage[],
  states: Record<string, StationState>,
  status: FactoryStatus | undefined,
  now: Date,
): IntentProgress {
  const previous = status?.previous ?? [];
  const started = previous.map((p) => p.started_at).filter((t): t is string => Boolean(t)).sort()[0];
  const finished = previous.map((p) => p.finished_at).filter((t): t is string => Boolean(t)).sort().at(-1);
  const active = Boolean(status?.run_status && ACTIVE_RUN.has(status.run_status));
  const end = !active && finished ? Date.parse(finished) : now.getTime();
  const costs = previous.map((p) => p.usd).filter((u): u is number => typeof u === "number");
  const current = stages.find((s) => states[s.id] === "running" || states[s.id] === "failed");
  return {
    done: stages.filter((s) => states[s.id] === "done").length,
    total: stages.length,
    currentStageId: current?.id ?? null,
    elapsedSeconds: started ? Math.max(0, Math.round((end - Date.parse(started)) / 1000)) : null,
    usd: costs.length ? costs.reduce((a, b) => a + b, 0) : null,
  };
}
