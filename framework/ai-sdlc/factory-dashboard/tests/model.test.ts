import { describe, expect, it } from "vitest";

import { isSafeLink } from "../src/ui/model.ts";
import type { LinePayload } from "../src/ui/model.ts";

const sampleLine: LinePayload = {
  schema_version: 1,
  stages: [
    {
      id: "plan",
      gates: [{ id: "spec", name: "Spec review", required: true, mode: "required" }],
      inputs: ["issue"],
      outputs: ["spec"],
      io_source: "default",
      agents: ["architect"],
      skills: ["openspec"],
      mcp: [],
      tools: [],
      model: "claude-sonnet-4-20250514",
      effort: { tokens: 50000 },
      guardrails: [],
    },
    {
      id: "red",
      gates: [],
      inputs: ["spec"],
      outputs: ["tests"],
      io_source: "default",
      agents: [],
      skills: [],
      mcp: [],
      tools: [],
      model: null,
      effort: {},
      guardrails: [],
    },
    {
      id: "green",
      gates: [{ id: "tests", name: "Tests pass", required: true, mode: "required" }, { id: "lint", name: "Lint", required: false, mode: "advisory" }],
      inputs: ["tests"],
      outputs: ["code"],
      io_source: "default",
      agents: ["coder"],
      skills: [],
      mcp: ["repolens"],
      tools: ["repolens"],
      model: null,
      effort: {},
      guardrails: [{ kind: "grant" as const, id: "g1", stage: "green", write_set: ["src/**"] }],
    },
  ],
  guardrails: {
    protected: { owner: "main", paths: ["src/**"] },
    hooks: ["pre-commit"],
    rules: [],
    isolator: { status: "active", sandbox: true, backend: "docker", enforces_committer: true },
  },
};

describe("LineStage carries metadata directly from /factory/line", () => {
  it("stages include agents, skills, mcp, tools, model, effort, guardrails", () => {
    const plan = sampleLine.stages.find((s) => s.id === "plan");
    expect(plan).toBeDefined();
    expect(plan!.agents).toEqual(["architect"]);
    expect(plan!.skills).toEqual(["openspec"]);
    expect(plan!.mcp).toEqual([]);
    expect(plan!.tools).toEqual([]);
    expect(plan!.model).toBe("claude-sonnet-4-20250514");
    expect(plan!.effort).toEqual({ tokens: 50000 });
    expect(plan!.guardrails).toEqual([]);
    expect(plan!.gates.length).toBe(1);
    expect(plan!.inputs).toEqual(["issue"]);

    const green = sampleLine.stages.find((s) => s.id === "green");
    expect(green!.agents).toEqual(["coder"]);
    expect(green!.mcp).toEqual(["repolens"]);
    expect(green!.tools).toEqual(["repolens"]);
    expect(green!.gates.length).toBe(2);
    expect(green!.guardrails.length).toBe(1);
    expect(green!.guardrails[0].kind).toBe("grant");

    const red = sampleLine.stages.find((s) => s.id === "red");
    expect(red!.agents).toEqual([]);
    expect(red!.skills).toEqual([]);
    expect(red!.mcp).toEqual([]);
    expect(red!.tools).toEqual([]);
    expect(red!.model).toBeNull();
    expect(red!.effort).toEqual({});
    expect(red!.guardrails).toEqual([]);
  });

  it("mergeLineConfig no longer exists", async () => {
    const model = await import("../src/ui/model.ts");
    expect("mergeLineConfig" in model).toBe(false);
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
