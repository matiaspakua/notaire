/**
 * #1577: client-side validation with the same rules as WorkflowValidationService:
 * exactly one start, at least one final, every node reachable from the start. Each problem
 * names the nodes it concerns so the canvas can highlight them.
 */
import { describe, it, expect } from "vitest";
import { validateWorkflow, invalidNodeIds } from "@/lib/workflow-validate";

const n = (id: number, type: string) => ({ id, type });
const t = (from: number, to: number) => ({ originNodeId: from, destinationNodeId: to });

describe("validateWorkflow (#1577)", () => {
  it("accepts start → mid → final", () => {
    const r = validateWorkflow([n(1, "INITIAL"), n(2, "INTERMEDIATE"), n(3, "FINAL")], [t(1, 2), t(2, 3)]);
    expect(r).toEqual({ valid: true, problems: [] });
  });

  it("flags an empty workflow", () => {
    expect(validateWorkflow([], []).problems).toEqual([{ code: "empty", nodeIds: [] }]);
  });

  it("flags a missing start", () => {
    const r = validateWorkflow([n(2, "INTERMEDIATE"), n(3, "FINAL")], [t(2, 3)]);
    expect(r.valid).toBe(false);
    expect(r.problems).toContainEqual({ code: "noStart", nodeIds: [] });
  });

  it("flags every start node when there are two", () => {
    const r = validateWorkflow([n(1, "INITIAL"), n(2, "INITIAL"), n(3, "FINAL")], [t(1, 3), t(2, 3)]);
    expect(r.problems).toContainEqual({ code: "multipleStart", nodeIds: [1, 2] });
  });

  it("flags a missing final", () => {
    const r = validateWorkflow([n(1, "INITIAL"), n(2, "INTERMEDIATE")], [t(1, 2)]);
    expect(r.problems).toContainEqual({ code: "noFinal", nodeIds: [] });
  });

  it("flags nodes unreachable from the start", () => {
    const r = validateWorkflow(
      [n(1, "INITIAL"), n(2, "INTERMEDIATE"), n(3, "FINAL"), n(4, "INTERMEDIATE")],
      [t(1, 2), t(2, 3), t(4, 3)],
    );
    expect(r.problems).toEqual([{ code: "unreachable", nodeIds: [4] }]);
  });

  it("like the backend, checks reachability only once start and final are right", () => {
    const r = validateWorkflow([n(1, "INITIAL"), n(2, "INTERMEDIATE")], []);
    expect(r.problems.map((p) => p.code)).toEqual(["noFinal"]);
  });

  it("collects the ids of every flagged node", () => {
    const r = validateWorkflow([n(1, "INITIAL"), n(2, "INITIAL"), n(3, "FINAL")], [t(1, 3)]);
    expect([...invalidNodeIds(r)].sort()).toEqual([1, 2]);
  });
});
