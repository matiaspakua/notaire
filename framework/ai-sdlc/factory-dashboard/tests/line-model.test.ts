import { describe, expect, it } from "vitest";

import {
  gateState,
  intentProgress,
  normalizeStage,
  stageOfStep,
  stationKind,
  stationStates,
  stationThroughput,
} from "../src/ui/line-model.ts";
import type { FactoryStatus, LineStage, TelemetryRow } from "../src/ui/model.ts";

function stage(id: string, agents: string[] = []): LineStage {
  return {
    id,
    gates: [],
    inputs: [],
    outputs: [],
    agents,
    skills: [],
    mcp: [],
    tools: [],
    model: null,
    effort: {},
    guardrails: [],
  };
}

const LINE = ["plan", "red", "pre-push", "pre-pr", "merge", "release", "signal"].map((id) =>
  stage(id, ["plan", "red", "pre-push", "pre-pr"].includes(id) ? ["worker"] : []),
);

const WORKFLOW_STEPS = [
  { id: "spec", stage: "plan" },
  { id: "red", stage: "red" },
  { id: "green", stage: "pre-push" },
  { id: "docs", stage: "pre-pr" },
];

function status(overrides: Partial<FactoryStatus>): FactoryStatus {
  return {
    intent: null,
    run: "issue-1",
    run_status: "running",
    stage: null,
    step: null,
    previous: [],
    next: [],
    updated_at: "2026-11-22T20:00:00+00:00",
    ...overrides,
  };
}

describe("stationKind", () => {
  it("names a stage with agents an AI agent", () => {
    expect(stationKind(stage("plan", ["openspec-planner"]))).toBe("agent");
  });

  it("names the merge stage a human decision", () => {
    expect(stationKind(stage("merge"))).toBe("human");
  });

  it("names any other stage a deterministic step", () => {
    expect(stationKind(stage("release"))).toBe("deterministic");
  });
});

describe("normalizeStage", () => {
  it("fills the fields an older kit omits with empty values", () => {
    const old = normalizeStage({ id: "plan", gates: [], inputs: ["issue"], outputs: [] });
    expect(old.agents).toEqual([]);
    expect(old.guardrails).toEqual([]);
    expect(old.effort).toEqual({});
    expect(old.model).toBeNull();
    expect(stationKind(old)).toBe("deterministic");
  });
});

describe("stageOfStep", () => {
  it("maps a workflow step to its stage", () => {
    expect(stageOfStep("green", WORKFLOW_STEPS, LINE)).toBe("pre-push");
  });

  it("falls back to a step named like a stage", () => {
    expect(stageOfStep("merge", [], LINE)).toBe("merge");
  });

  it("returns null for an unknown step", () => {
    expect(stageOfStep("archive", WORKFLOW_STEPS, LINE)).toBeNull();
  });
});

describe("stationStates", () => {
  it("leaves every station idle without a run", () => {
    const states = stationStates(LINE, undefined, WORKFLOW_STEPS);
    expect(Object.values(states).every((s) => s === "idle")).toBe(true);
  });

  it("puts the running stage between done and next stations", () => {
    const states = stationStates(LINE, status({ stage: "red", step: "red" }), WORKFLOW_STEPS);
    expect(states.plan).toBe("done");
    expect(states.red).toBe("running");
    expect(states["pre-push"]).toBe("next");
    expect(states.signal).toBe("next");
  });

  it("derives the current stage from the step when the status has no stage", () => {
    const states = stationStates(LINE, status({ stage: null, step: "green" }), WORKFLOW_STEPS);
    expect(states.red).toBe("done");
    expect(states["pre-push"]).toBe("running");
  });

  it("marks done what previous steps reached when the current step maps to no stage", () => {
    const running = status({
      stage: null,
      step: "implement",
      previous: [
        { step: "spec", stage: null, status: "pass" },
        { step: "red", stage: "red", status: "pass" },
      ],
    });
    const states = stationStates(LINE, running, WORKFLOW_STEPS);
    expect(states.plan).toBe("done");
    expect(states.red).toBe("done");
    expect(states["pre-push"]).toBe("next");
    expect(Object.values(states)).not.toContain("running");
  });

  it("marks the station of a failed run as failed", () => {
    const states = stationStates(LINE, status({ run_status: "failed", stage: "pre-push" }), WORKFLOW_STEPS);
    expect(states.red).toBe("done");
    expect(states["pre-push"]).toBe("failed");
    expect(states["pre-pr"]).toBe("next");
  });

  it("marks done every station a succeeded run reached and leaves the rest idle", () => {
    const finished = status({
      run_status: "succeeded",
      previous: [
        { step: "spec", stage: null, status: "pass" },
        { step: "red", stage: null, status: "pass" },
        { step: "docs", stage: null, status: "pass" },
      ],
    });
    const states = stationStates(LINE, finished, WORKFLOW_STEPS);
    expect(states.plan).toBe("done");
    expect(states["pre-pr"]).toBe("done");
    expect(states.merge).toBe("idle");
  });
});

describe("gateState", () => {
  it("never shows a skipped gate as passed", () => {
    expect(gateState("skip")).toBe("not-evaluable");
    expect(gateState("todo")).toBe("not-evaluable");
  });

  it("maps pass and fail", () => {
    expect(gateState("pass")).toBe("passed");
    expect(gateState("fail")).toBe("failed");
  });

  it("says a gate without evidence has not run", () => {
    expect(gateState(undefined)).toBe("not-run");
  });
});

describe("stationThroughput", () => {
  const rows: TelemetryRow[] = [
    { key: "spec", runs: 2, duration_s: 60, usd: 0.2, failures: 0 },
    { key: "plan", runs: 1, duration_s: 30, usd: 0.1, failures: 1 },
    { key: "green", runs: 4, duration_s: 400, usd: 0, failures: 0 },
  ];

  it("sums step rows into their station", () => {
    expect(stationThroughput("plan", rows, WORKFLOW_STEPS, LINE)).toEqual({
      runs: 3,
      avgSeconds: 30,
      usd: 0.30000000000000004,
      failures: 1,
    });
  });

  it("treats rows with zero runs as no runs", () => {
    const empty: TelemetryRow[] = [{ key: "signal", runs: 0, duration_s: 0, usd: 0, failures: 0 }];
    expect(stationThroughput("signal", empty, WORKFLOW_STEPS, LINE)).toBeNull();
  });

  it("returns null when no row maps to the station", () => {
    expect(stationThroughput("release", rows, WORKFLOW_STEPS, LINE)).toBeNull();
  });
});

describe("intentProgress", () => {
  const now = new Date("2026-11-22T20:10:00+00:00");

  it("counts stations complete and sums reported cost", () => {
    const running = status({
      stage: "pre-push",
      previous: [
        { step: "spec", stage: "plan", status: "pass", started_at: "2026-11-22T20:00:00+00:00", usd: 0.2 },
        { step: "red", stage: "red", status: "pass", usd: 0.42 },
      ],
    });
    const states = stationStates(LINE, running, WORKFLOW_STEPS);
    const progress = intentProgress(LINE, states, running, now);
    expect(progress.done).toBe(2);
    expect(progress.total).toBe(7);
    expect(progress.currentStageId).toBe("pre-push");
    expect(progress.elapsedSeconds).toBe(600);
    expect(progress.usd).toBeCloseTo(0.62);
  });

  it("stops the clock at the last finished step of a finished run", () => {
    const finished = status({
      run_status: "succeeded",
      previous: [
        { step: "spec", stage: "plan", status: "pass", started_at: "2026-11-22T20:00:00+00:00", finished_at: "2026-11-22T20:03:00+00:00" },
        { step: "red", stage: "red", status: "pass", started_at: "2026-11-22T20:03:00+00:00", finished_at: "2026-11-22T20:05:00+00:00" },
      ],
    });
    const progress = intentProgress(LINE, stationStates(LINE, finished, WORKFLOW_STEPS), finished, now);
    expect(progress.elapsedSeconds).toBe(300);
  });

  it("reports unknown cost as null, not zero", () => {
    const running = status({ stage: "red", previous: [{ step: "spec", stage: "plan", status: "pass", usd: null }] });
    const progress = intentProgress(LINE, stationStates(LINE, running, WORKFLOW_STEPS), running, now);
    expect(progress.usd).toBeNull();
    expect(progress.elapsedSeconds).toBeNull();
  });
});
