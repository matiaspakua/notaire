/**
 * #1574: automatic layout for the workflow editor. Saved positions win; nodes without one are
 * laid out left-to-right following the transitions; no two boxes overlap.
 */
import { describe, it, expect } from "vitest";
import { layoutWorkflow, nextFreePosition, NODE_WIDTH, NODE_HEIGHT } from "@/lib/workflow-layout";
import type { WorkflowNode, WorkflowTransition } from "@/types";

const node = (id: number, x: number | null = null, y: number | null = null): WorkflowNode =>
  ({ id, positionX: x, positionY: y, type: "INTERMEDIATE", statusManagementName: `S${id}` }) as WorkflowNode;
const tr = (id: number, from: number, to: number): WorkflowTransition =>
  ({ id, originNodeId: from, destinationNodeId: to }) as WorkflowTransition;

function overlaps(a: { x: number; y: number }, b: { x: number; y: number }) {
  return a.x < b.x + NODE_WIDTH && b.x < a.x + NODE_WIDTH && a.y < b.y + NODE_HEIGHT && b.y < a.y + NODE_HEIGHT;
}
function assertNoOverlap(pos: Map<string, { x: number; y: number }>) {
  const list = [...pos.values()];
  for (let i = 0; i < list.length; i++) for (let j = i + 1; j < list.length; j++) expect(overlaps(list[i], list[j])).toBe(false);
}

describe("layoutWorkflow (#1574)", () => {
  it("lays out nodes without positions with no overlap, ranks following transitions", () => {
    const nodes = [node(1), node(2), node(3), node(4)];
    const pos = layoutWorkflow(nodes, [tr(1, 1, 2), tr(2, 2, 3), tr(3, 2, 4)]);
    expect(pos.size).toBe(4);
    assertNoOverlap(pos);
    expect(pos.get("1")!.x).toBeLessThan(pos.get("2")!.x);
    expect(pos.get("2")!.x).toBeLessThan(pos.get("3")!.x);
  });

  it("keeps saved positions and only places the missing ones, without overlap", () => {
    const nodes = [node(1, 0, 0), node(2, 300, 0), node(3)];
    const pos = layoutWorkflow(nodes, [tr(1, 1, 2), tr(2, 2, 3)]);
    expect(pos.get("1")).toEqual({ x: 0, y: 0 });
    expect(pos.get("2")).toEqual({ x: 300, y: 0 });
    assertNoOverlap(pos);
  });

  it("separates saved positions that overlap (e.g. API default 0,0)", () => {
    const nodes = [node(1, 0, 0), node(2, 0, 0), node(3, 0, 0)];
    const pos = layoutWorkflow(nodes, [tr(1, 1, 2), tr(2, 2, 3)]);
    expect(pos.get("1")).toEqual({ x: 0, y: 0 });
    assertNoOverlap(pos);
  });

  it("force re-lays out every node", () => {
    const nodes = [node(1, 0, 0), node(2, 0, 0)];
    const pos = layoutWorkflow(nodes, [tr(1, 1, 2)], { force: true });
    assertNoOverlap(pos);
  });

  it("handles cycles and isolated nodes", () => {
    const pos = layoutWorkflow([node(1), node(2), node(3)], [tr(1, 1, 2), tr(2, 2, 1)]);
    expect(pos.size).toBe(3);
    assertNoOverlap(pos);
  });
});

describe("nextFreePosition (#1574)", () => {
  it("places a new node right of the current bounding box", () => {
    const p = nextFreePosition([{ x: 0, y: 0 }, { x: 400, y: 100 }]);
    expect(p.x).toBeGreaterThanOrEqual(400 + NODE_WIDTH);
  });
  it("starts at the origin area for an empty workflow", () => {
    expect(nextFreePosition([])).toEqual({ x: 0, y: 0 });
  });
});
