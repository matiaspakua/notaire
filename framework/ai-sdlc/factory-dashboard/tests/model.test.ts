import { describe, expect, it } from "vitest";

import { isSafeLink, mergeLineConfig, stationState } from "../src/ui/model.ts";
import type { ConfigPayload, FactoryStatus, LinePayload } from "../src/ui/model.ts";

const sampleLine: LinePayload = {
  schema_version: 1,
  stages: [
    { id: "plan", gates: [{ id: "spec", name: "Spec review", required: true, mode: "required" }], inputs: ["issue"], outputs: ["spec"] },
    { id: "red", gates: [], inputs: ["spec"], outputs: ["tests"] },
    { id: "green", gates: [{ id: "tests", name: "Tests pass", required: true, mode: "required" }, { id: "lint", name: "Lint", required: false, mode: "advisory" }], inputs: ["tests"], outputs: ["code"] },
  ],
  guardrails: {
    protected: { owner: "main", paths: ["src/**"] },
    hooks: ["pre-commit"],
    rules: [],
    isolator: { status: "active", sandbox: true, backend: "docker", enforces_committer: true },
  },
};

const sampleConfig: ConfigPayload = {
  stages: [
    { id: "plan", agents: ["architect"], skills: ["openspec"], mcp: [], model: "claude-sonnet-4-20250514", effort: { tokens: 50000 }, inputs: [], outputs: [] },
    { id: "green", agents: ["coder"], skills: [], mcp: ["repolens"], model: null, effort: {}, inputs: [], outputs: [] },
  ],
};

describe("mergeLineConfig", () => {
  it("merges config stage details into line stages by id", () => {
    const merged = mergeLineConfig(sampleLine, sampleConfig);
    expect(merged.length).toBe(3);

    const plan = merged.find((s) => s.id === "plan") as any;
    expect(plan.agents).toEqual(["architect"]);
    expect(plan.skills).toEqual(["openspec"]);
    expect(plan.model).toBe("claude-sonnet-4-20250514");
    expect(plan.effort).toEqual({ tokens: 50000 });
    expect(plan.gates.length).toBe(1);
    expect(plan.inputs).toEqual(["issue"]);

    const green = merged.find((s) => s.id === "green") as any;
    expect(green.agents).toEqual(["coder"]);
    expect(green.mcp).toEqual(["repolens"]);
    expect(green.gates.length).toBe(2);

    const red = merged.find((s) => s.id === "red") as any;
    expect(red.agents).toEqual([]);
    expect(red.skills).toEqual([]);
    expect(red.effort).toEqual({});
  });

  it("tolerates missing config", () => {
    const merged = mergeLineConfig(sampleLine, undefined);
    expect(merged.length).toBe(3);
    for (const s of merged as any[]) {
      expect(s.agents).toEqual([]);
      expect(s.skills).toEqual([]);
      expect(s.mcp).toEqual([]);
      expect(s.effort).toEqual({});
    }
  });

  it("tolerates empty config", () => {
    const merged = mergeLineConfig(sampleLine, { stages: [] });
    expect(merged.length).toBe(3);
    for (const s of merged as any[]) {
      expect(s.agents).toEqual([]);
    }
  });
});

describe("stationState", () => {
  it("returns 'current' when status stage matches and run is running", () => {
    const status: FactoryStatus = {
      intent: null, run: "r1", workflow: null, run_status: "running",
      stage: "green", step: "impl", previous: [], next: [], updated_at: null,
    };
    expect(stationState("green", status)).toBe("current");
    expect(stationState("plan", status)).toBe("upcoming");
  });

  it("returns 'done' when the stage appears in previous", () => {
    const status: FactoryStatus = {
      intent: null, run: "r1", workflow: null, run_status: "running",
      stage: "green", step: "impl",
      previous: [{ step: "s1", stage: "plan", status: "pass" }],
      next: [], updated_at: null,
    };
    expect(stationState("plan", status)).toBe("done");
    expect(stationState("green", status)).toBe("current");
  });

  it("returns 'upcoming' when no status", () => {
    expect(stationState("plan", undefined)).toBe("upcoming");
    expect(stationState("plan", null as any)).toBe("upcoming");
  });

  it("returns 'upcoming' when run_status is null", () => {
    const status: FactoryStatus = {
      intent: null, run: null, workflow: null, run_status: null,
      stage: null, step: null, previous: [], next: [], updated_at: null,
    };
    expect(stationState("plan", status)).toBe("upcoming");
  });

  it("prefers current over done when stage matches", () => {
    const status: FactoryStatus = {
      intent: null, run: "r1", workflow: null, run_status: "running",
      stage: "plan", step: "spec",
      previous: [{ step: "s1", stage: "plan", status: "pass" }],
      next: [], updated_at: null,
    };
    expect(stationState("plan", status)).toBe("current");
  });
});

describe("isSafeLink", () => {
  it("accepts http and https URLs", () => {
    expect(isSafeLink("https://github.com/example/repo/issues/2400")).toBe(true);
    expect(isSafeLink("http://example.com")).toBe(true);
  });

  it("rejects non-http schemes", () => {
    expect(isSafeLink("javascript:alert(1)")).toBe(false);
    expect(isSafeLink("file:///etc/passwd")).toBe(false);
    expect(isSafeLink("data:text/html,<h1>hi</h1>")).toBe(false);
  });

  it("rejects invalid URLs", () => {
    expect(isSafeLink("not-a-url")).toBe(false);
    expect(isSafeLink("")).toBe(false);
  });
});
