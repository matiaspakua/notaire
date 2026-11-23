import { describe, expect, it } from "vitest";

import { commitUrl, fileUrl, isEmptyIo, repoBase, safeUrl, stepsAtStage } from "../src/ui/step-io.ts";
import type { LineStage } from "../src/ui/model.ts";

const stage = (id: string): LineStage => ({
  id, gates: [], inputs: [], outputs: [], agents: [], skills: [], mcp: [], tools: [], model: null, effort: {}, guardrails: [],
});
const STAGES = ["plan", "red", "pre-push"].map(stage);

describe("step inputs and outputs (#2435)", () => {
  it("accepts only http and https links", () => {
    expect(safeUrl("https://github.com/o/r/pull/1")).toBe("https://github.com/o/r/pull/1");
    expect(safeUrl("http://example.invalid/x")).toBe("http://example.invalid/x");
    expect(safeUrl("javascript:alert(1)")).toBeNull();
    expect(safeUrl("data:text/html,x")).toBeNull();
    expect(safeUrl("not a url")).toBeNull();
  });

  it("derives the repository base from a project URL", () => {
    expect(repoBase("https://github.com/o/r.git")).toBe("https://github.com/o/r");
    expect(repoBase("https://github.com/o/r/")).toBe("https://github.com/o/r");
    expect(repoBase("git@github.com:o/r.git")).toBeNull();
    expect(repoBase(undefined)).toBeNull();
  });

  it("links files at a revision and commits by SHA", () => {
    const base = "https://github.com/o/r";
    expect(fileUrl(base, "docs/a b.md", "abc123")).toBe("https://github.com/o/r/blob/abc123/docs/a%20b.md");
    expect(fileUrl(base, "README.md")).toBe("https://github.com/o/r/blob/HEAD/README.md");
    expect(fileUrl(null, "README.md")).toBeNull();
    expect(commitUrl(base, "0123abcd")).toBe("https://github.com/o/r/commit/0123abcd");
    expect(commitUrl(null, "0123abcd")).toBeNull();
  });

  it("knows when a side recorded nothing", () => {
    expect(isEmptyIo(undefined)).toBe(true);
    expect(isEmptyIo({ files: [], links: [] })).toBe(true);
    expect(isEmptyIo({ prompt: "x" })).toBe(false);
  });

  it("finds the steps of a stage by their own stage or the workflow mapping", () => {
    const steps = [
      { id: "spec", status: "pass", stage: "plan" },
      { id: "red", status: "running" },
      { id: "green", status: "pending" },
    ];
    const workflow = [{ id: "green", stage: "pre-push" }];
    expect(stepsAtStage(steps, "plan", workflow, STAGES).map((s) => s.id)).toEqual(["spec"]);
    expect(stepsAtStage(steps, "red", workflow, STAGES).map((s) => s.id)).toEqual(["red"]);
    expect(stepsAtStage(steps, "pre-push", workflow, STAGES).map((s) => s.id)).toEqual(["green"]);
  });
});
