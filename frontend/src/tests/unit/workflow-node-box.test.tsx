/**
 * #1575: status boxes with a readable type badge (not color-only) and directional arrows.
 */
import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { ReactFlowProvider, MarkerType } from "@xyflow/react";

vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string, vals?: Record<string, unknown>) =>
    vals ? `${ns}.${key}:${Object.values(vals).join("|")}` : `${ns}.${key}`,
}));

import { WorkflowNodeBox } from "@/components/workflow/WorkflowNodeBox";
import { workflowEdge } from "@/components/workflow/workflow-edges";

function box(type: string, selected = false) {
  return render(
    <ReactFlowProvider>
      <WorkflowNodeBox
        {...({ id: "7", data: { label: "Testimonio Ingresado a Inscripcion", type }, selected } as unknown as Parameters<typeof WorkflowNodeBox>[0])}
      />
    </ReactFlowProvider>,
  );
}

describe("WorkflowNodeBox (#1575)", () => {
  it("shows the status name and a text type badge", () => {
    box("INITIAL");
    const node = screen.getByTestId("workflow-node-7");
    expect(node).toHaveTextContent("Testimonio Ingresado a Inscripcion");
    expect(screen.getByTestId("workflow-node-badge-7")).toHaveTextContent("workflowViewer.types.INITIAL");
  });

  it("has an accessible name with the status and its type", () => {
    box("FINAL");
    expect(screen.getByTestId("workflow-node-7")).toHaveAttribute(
      "aria-label",
      "workflowViewer.nodeAria:Testimonio Ingresado a Inscripcion|workflowViewer.types.FINAL",
    );
  });

  it("marks start and final nodes with a distinct shape, not only color", () => {
    box("INITIAL");
    expect(screen.getByTestId("workflow-node-7").dataset.shape).toBe("start");
  });

  it("highlights the selected node", () => {
    box("INTERMEDIATE", true);
    expect(screen.getByTestId("workflow-node-7").dataset.selected).toBe("true");
  });
});

describe("workflowEdge (#1575)", () => {
  it("draws a smooth-step arrow with a closed arrowhead and a readable label", () => {
    const e = workflowEdge({ id: 3, originNodeId: 1, destinationNodeId: 2, description: "pasa" });
    expect(e.type).toBe("smoothstep");
    expect((e.markerEnd as { type: string }).type).toBe(MarkerType.ArrowClosed);
    expect(e.label).toBe("pasa");
    expect(e.labelBgStyle).toBeDefined();
    expect(e.source).toBe("1");
    expect(e.target).toBe("2");
  });
});
