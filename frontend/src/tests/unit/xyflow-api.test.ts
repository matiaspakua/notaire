import * as xyflow from "@xyflow/react";
import { describe, expect, it } from "vitest";

// The workflow editor applies node changes as an array (#2440). @xyflow/react 13 removed that
// API; this guard fails before the build does if a major upgrade lands without the migration.
describe("@xyflow/react API used by the workflow editor (#2440)", () => {
  it("provides applyNodeChanges", () => {
    expect(typeof (xyflow as Record<string, unknown>).applyNodeChanges).toBe("function");
  });
});
