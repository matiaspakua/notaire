/**
 * #1579: zoom is bounded, the canvas controls have localized accessible names, and the
 * MiniMap colors boxes by type and hides on small screens.
 */
import { describe, it, expect } from "vitest";
import { theme } from "@/theme/tokens";
import {
  WORKFLOW_ZOOM,
  workflowAriaLabels,
  workflowMiniMapColor,
  MINIMAP_CLASS,
} from "@/components/workflow/workflow-canvas";

const t = (key: string) => `T:${key}`;

describe("workflow canvas chrome (#1579)", () => {
  it("bounds zoom between 0.2 and 2", () => {
    expect(WORKFLOW_ZOOM).toEqual({ minZoom: 0.2, maxZoom: 2 });
  });

  it("gives every control a localized accessible name", () => {
    const labels = workflowAriaLabels(t);
    expect(labels["controls.zoomIn.ariaLabel"]).toBe("T:zoomIn");
    expect(labels["controls.zoomOut.ariaLabel"]).toBe("T:zoomOut");
    expect(labels["controls.fitView.ariaLabel"]).toBe("T:fitView");
    expect(labels["controls.interactive.ariaLabel"]).toBe("T:interactive");
    expect(labels["controls.ariaLabel"]).toBe("T:controls");
    expect(labels["minimap.ariaLabel"]).toBe("T:minimap");
  });

  it("colors MiniMap boxes by node type", () => {
    expect(workflowMiniMapColor({ data: { type: "INITIAL" } })).toBe(theme.colors.success[500]);
    expect(workflowMiniMapColor({ data: { type: "FINAL" } })).toBe(theme.colors.error[500]);
    expect(workflowMiniMapColor({ data: {} })).toBe(theme.colors.primary[500]);
  });

  it("hides the MiniMap below the md breakpoint", () => {
    expect(MINIMAP_CLASS).toContain("max-md:hidden");
  });
});
