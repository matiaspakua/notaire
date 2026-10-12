/**
 * #1576: a new transition is rejected before the API call when it is a self-loop or a duplicate.
 */
import { describe, it, expect } from "vitest";
import { checkNewTransition } from "@/lib/workflow-edit";

const transitions = [{ id: 1, originNodeId: 10, destinationNodeId: 11 }];

describe("checkNewTransition (#1576)", () => {
  it("accepts a new pair", () => {
    expect(checkNewTransition(transitions, 11, 12)).toBeNull();
  });
  it("accepts the reverse direction of an existing transition", () => {
    expect(checkNewTransition(transitions, 11, 10)).toBeNull();
  });
  it("rejects a self-loop", () => {
    expect(checkNewTransition(transitions, 10, 10)).toBe("selfLoop");
  });
  it("rejects a duplicate", () => {
    expect(checkNewTransition(transitions, 10, 11)).toBe("duplicate");
  });
  it("rejects an incomplete pair", () => {
    expect(checkNewTransition(transitions, 10, undefined)).toBe("incomplete");
  });
});
