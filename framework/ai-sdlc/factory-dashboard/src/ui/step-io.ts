import { stageOfStep, type WorkflowStep } from "./line-model.ts";
import type { LineStage, RunStep, StepIo } from "./model.ts";

const WEB = new Set(["http:", "https:"]);

export function safeUrl(url: string | undefined | null): string | null {
  if (!url) return null;
  try {
    return WEB.has(new URL(url).protocol) ? url : null;
  } catch {
    return null;
  }
}

export function repoBase(url: string | undefined | null): string | null {
  const safe = safeUrl(url);
  return safe ? safe.replace(/\/+$/, "").replace(/\.git$/, "") : null;
}

const encodePath = (path: string) => path.split("/").map(encodeURIComponent).join("/");

export function fileUrl(base: string | null, path: string, ref?: string): string | null {
  return base ? `${base}/blob/${encodeURIComponent(ref ?? "HEAD")}/${encodePath(path)}` : null;
}

export function commitUrl(base: string | null, sha: string): string | null {
  return base ? `${base}/commit/${encodeURIComponent(sha)}` : null;
}

export function isEmptyIo(io: StepIo | undefined): boolean {
  if (!io) return true;
  return !io.request && !io.prompt && !io.summary && !io.files?.length && !io.commits?.length && !io.links?.length;
}

export function stepsAtStage(steps: RunStep[], stageId: string, workflow: WorkflowStep[], stages: LineStage[]): RunStep[] {
  return steps.filter((step) => {
    const own = step.stage && stages.some((s) => s.id === step.stage) ? step.stage : null;
    return (own ?? stageOfStep(step.id, workflow, stages)) === stageId;
  });
}
