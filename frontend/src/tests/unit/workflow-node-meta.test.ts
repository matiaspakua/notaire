import { describe, it, expect } from "vitest";
import { theme } from "@/theme/tokens";
import { NODE_META, getNodeMeta, withNodeIcon } from "@/lib/workflow-node-meta";

describe("workflow node meta (#646)", () => {
  it("sources every node type's colors from theme tokens, not hardcoded hex", () => {
    expect(NODE_META.INITIAL.background).toBe(theme.colors.success[50]);
    expect(NODE_META.INITIAL.border).toBe(theme.colors.success[500]);

    expect(NODE_META.INTERMEDIATE.background).toBe(theme.colors.primary[50]);
    expect(NODE_META.INTERMEDIATE.border).toBe(theme.colors.primary[500]);

    expect(NODE_META.FINAL.background).toBe(theme.colors.error[50]);
    expect(NODE_META.FINAL.border).toBe(theme.colors.error[500]);
  });

  it("falls back to INTERMEDIATE meta for an unknown or missing tipo", () => {
    expect(getNodeMeta(undefined)).toBe(NODE_META.INTERMEDIATE);
    expect(getNodeMeta("SOMETHING_ELSE")).toBe(NODE_META.INTERMEDIATE);
  });

  it("differentiates each node type with a distinct icon, not color alone", () => {
    const icons = new Set([NODE_META.INITIAL.icon, NODE_META.INTERMEDIATE.icon, NODE_META.FINAL.icon]);
    expect(icons.size).toBe(3);
  });

  it("prefixes a label with the type's icon", () => {
    expect(withNodeIcon("Borrador", "INITIAL")).toBe(`${NODE_META.INITIAL.icon} Borrador`);
    expect(withNodeIcon("Firmado", "FINAL")).toBe(`${NODE_META.FINAL.icon} Firmado`);
  });
});
