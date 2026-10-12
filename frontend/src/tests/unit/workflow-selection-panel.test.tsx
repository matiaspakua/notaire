/**
 * #1576: editing through visible controls. The panel calls the right handlers, every delete
 * is guarded by a confirmation, and invalid transitions never reach the API.
 */
import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";

vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string, vals?: Record<string, unknown>) =>
    vals ? `${ns}.${key}:${Object.values(vals).join("|")}` : `${ns}.${key}`,
}));

import { WorkflowSelectionPanel, type PanelProps } from "@/components/workflow/WorkflowSelectionPanel";

const nodes = [
  { id: 10, label: "Ingresado", type: "INITIAL" },
  { id: 11, label: "En curso", type: "INTERMEDIATE" },
  { id: 12, label: "Cerrado", type: "FINAL" },
];
const transitions = [{ id: 1, originNodeId: 10, destinationNodeId: 11, description: "iniciar" }];

function setup(over: Partial<PanelProps> = {}) {
  const props: PanelProps = {
    selection: null,
    nodes,
    transitions,
    editMode: true,
    onChangeType: vi.fn(),
    onDeleteNode: vi.fn(),
    onDeleteTransition: vi.fn(),
    onAddTransition: vi.fn(),
    onSaveDescription: vi.fn(),
    ...over,
  };
  render(<WorkflowSelectionPanel {...props} />);
  return props;
}

describe("WorkflowSelectionPanel (#1576)", () => {
  it("prompts to select something when nothing is selected", () => {
    setup();
    expect(screen.getByTestId("wf-panel-empty")).toBeInTheDocument();
  });

  it("shows a node's status, type and its transitions", () => {
    setup({ selection: { kind: "node", id: 11 } });
    expect(screen.getByTestId("wf-panel-node")).toHaveTextContent("En curso");
    expect(screen.getByTestId("wf-panel-type")).toHaveValue("INTERMEDIATE");
    expect(screen.getByTestId("wf-panel-incoming")).toHaveTextContent("Ingresado");
  });

  it("changes the node type", () => {
    const p = setup({ selection: { kind: "node", id: 11 } });
    fireEvent.change(screen.getByTestId("wf-panel-type"), { target: { value: "FINAL" } });
    expect(p.onChangeType).toHaveBeenCalledWith(11, "FINAL");
  });

  it("asks for confirmation before deleting a node", () => {
    const p = setup({ selection: { kind: "node", id: 12 } });
    fireEvent.click(screen.getByTestId("wf-panel-delete-node"));
    expect(p.onDeleteNode).not.toHaveBeenCalled();
    fireEvent.click(screen.getByTestId("confirm-dialog-confirm"));
    expect(p.onDeleteNode).toHaveBeenCalledWith(12);
  });

  it("warns that a node with transitions cannot be deleted yet", () => {
    setup({ selection: { kind: "node", id: 10 } });
    fireEvent.click(screen.getByTestId("wf-panel-delete-node"));
    expect(screen.getByRole("alertdialog")).toHaveTextContent("deleteNodeHasTransitions");
  });

  it("adds a transition from the selected node", () => {
    const p = setup({ selection: { kind: "node", id: 11 } });
    fireEvent.change(screen.getByTestId("wf-panel-add-to"), { target: { value: "12" } });
    fireEvent.change(screen.getByTestId("wf-panel-add-desc"), { target: { value: "cerrar" } });
    fireEvent.click(screen.getByTestId("wf-panel-add-transition"));
    expect(p.onAddTransition).toHaveBeenCalledWith(11, 12, "cerrar");
  });

  it("rejects a duplicate transition with a message", () => {
    const p = setup({ selection: { kind: "node", id: 10 } });
    fireEvent.change(screen.getByTestId("wf-panel-add-to"), { target: { value: "11" } });
    fireEvent.click(screen.getByTestId("wf-panel-add-transition"));
    expect(p.onAddTransition).not.toHaveBeenCalled();
    expect(screen.getByTestId("wf-panel-add-error")).toHaveTextContent("duplicate");
  });

  it("shows a transition and saves its description", () => {
    const p = setup({ selection: { kind: "edge", id: 1 } });
    expect(screen.getByTestId("wf-panel-edge")).toHaveTextContent("Ingresado");
    expect(screen.getByTestId("wf-panel-edge")).toHaveTextContent("En curso");
    fireEvent.change(screen.getByTestId("wf-panel-desc"), { target: { value: "comenzar" } });
    fireEvent.click(screen.getByTestId("wf-panel-save-desc"));
    expect(p.onSaveDescription).toHaveBeenCalledWith(1, "comenzar");
  });

  it("asks for confirmation before deleting a transition", () => {
    const p = setup({ selection: { kind: "edge", id: 1 } });
    fireEvent.click(screen.getByTestId("wf-panel-delete-edge"));
    expect(p.onDeleteTransition).not.toHaveBeenCalled();
    fireEvent.click(screen.getByTestId("confirm-dialog-confirm"));
    expect(p.onDeleteTransition).toHaveBeenCalledWith(1);
  });

  it("is read-only outside edit mode", () => {
    setup({ selection: { kind: "node", id: 11 }, editMode: false });
    expect(screen.getByTestId("wf-panel-type")).toBeDisabled();
    expect(screen.queryByTestId("wf-panel-delete-node")).not.toBeInTheDocument();
  });
});
