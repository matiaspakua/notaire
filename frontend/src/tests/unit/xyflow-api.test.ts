import * as xyflow from "@xyflow/react";
import { describe, expect, it } from "vitest";

// The workflow editor applies node changes with NodeChangeset.applyTo (@xyflow/react 13, #2441).
// This guard fails before the build does if the installed version lacks that API.
describe("@xyflow/react API used by the workflow editor (#2441)", () => {
  it("applies a position change through NodeChangeset.applyTo", () => {
    const { NodeChangeset } = xyflow as unknown as {
      NodeChangeset?: new (changes: unknown[]) => { applyTo: (nodes: unknown[]) => { id: string; position: unknown }[] };
    };
    expect(typeof NodeChangeset).toBe("function");
    const nodes = [{ id: "a", position: { x: 0, y: 0 }, data: {} }];
    const next = new NodeChangeset!([{ id: "a", type: "position", position: { x: 40, y: 20 } }]).applyTo(nodes);
    expect(next[0]?.position).toEqual({ x: 40, y: 20 });
  });
});
