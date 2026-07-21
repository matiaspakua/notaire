import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { theme } from "@/theme/tokens";
import { WorkflowViewer, toFlowNodes, toFlowEdges } from "@/components/shared/WorkflowViewer";
import type { WorkflowNode, WorkflowTransition } from "@/types";

const node = (id: number, tipo: string): WorkflowNode => ({
  id,
  estadoGestionNombre: `Estado ${id}`,
  tipo,
  posicionX: 0,
  posicionY: 0,
} as WorkflowNode);

describe("WorkflowViewer node styling (#613)", () => {
  it("sources node colors from theme tokens, not hardcoded hex values", () => {
    const [initial, intermediate, final] = toFlowNodes([
      node(1, "INITIAL"),
      node(2, "INTERMEDIATE"),
      node(3, "FINAL"),
    ]);

    expect(initial.style?.background).toBe(theme.colors.success[50]);
    expect(initial.style?.border).toContain(theme.colors.success[500]);

    expect(intermediate.style?.background).toBe(theme.colors.primary[50]);
    expect(intermediate.style?.border).toContain(theme.colors.primary[500]);

    expect(final.style?.background).toBe(theme.colors.error[50]);
    expect(final.style?.border).toContain(theme.colors.error[500]);
  });

  it("differentiates node state with a text/icon badge, not color alone", () => {
    const [initial, intermediate, final] = toFlowNodes([
      node(1, "INITIAL"),
      node(2, "INTERMEDIATE"),
      node(3, "FINAL"),
    ]);

    const initialLabel = String((initial.data as { label: string }).label);
    const intermediateLabel = String((intermediate.data as { label: string }).label);
    const finalLabel = String((final.data as { label: string }).label);

    expect(initialLabel).not.toBe("Estado 1");
    expect(finalLabel).not.toBe("Estado 3");
    expect(initialLabel).not.toBe(intermediateLabel.replace("Estado 2", "Estado 1"));
    expect(initialLabel.charAt(0)).not.toBe(finalLabel.charAt(0));
    expect(initialLabel.charAt(0)).not.toBe(intermediateLabel.charAt(0));
  });

  it("sources the edge stroke color from theme tokens", () => {
    const transition: WorkflowTransition = {
      id: 1,
      nodoOrigenId: 1,
      nodoDestinoId: 2,
      descripcion: "next",
    } as WorkflowTransition;

    const [edge] = toFlowEdges([transition]);
    expect(edge.style?.stroke).toBe(theme.colors.neutral[500]);
  });

  it("renders the empty state without any nodes", () => {
    render(<WorkflowViewer nodes={[]} transitions={[]} data-testid="wf-viewer" />);
    expect(screen.getByTestId("wf-viewer")).toBeInTheDocument();
  });
});
