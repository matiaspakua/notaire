/**
 * #1578: list view of the workflow, an accessible, keyboard-operable alternative to the graph.
 */
import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent, within } from "@testing-library/react";

vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string, vals?: Record<string, unknown>) =>
    vals ? `${ns}.${key}:${Object.values(vals).join("|")}` : `${ns}.${key}`,
}));

import { WorkflowListView, type ListViewProps } from "@/components/workflow/WorkflowListView";

const nodes = [
  { id: 10, label: "Ingresado", type: "INITIAL" },
  { id: 11, label: "En curso", type: "INTERMEDIATE", problem: "unreachable" },
  { id: 12, label: "Cerrado", type: "FINAL" },
];
const transitions = [{ id: 1, originNodeId: 10, destinationNodeId: 12, description: "cerrar" }];
const statuses = [{ id: 5, name: "Nuevo estado" }];

function setup(over: Partial<ListViewProps> = {}) {
  const props: ListViewProps = {
    nodes, transitions, statuses, editMode: true,
    onAddNode: vi.fn(), onChangeType: vi.fn(), onDeleteNode: vi.fn(),
    onAddTransition: vi.fn(), onSaveDescription: vi.fn(), onDeleteTransition: vi.fn(),
    ...over,
  };
  render(<WorkflowListView {...props} />);
  return props;
}

describe("WorkflowListView (#1578)", () => {
  it("lists statuses with type, outgoing count and problem", () => {
    setup();
    const row = screen.getByTestId("wf-list-node-11");
    expect(row).toHaveTextContent("En curso");
    expect(within(row).getByRole("combobox")).toHaveValue("INTERMEDIATE");
    expect(row).toHaveTextContent("workflowViewer.problem.unreachable");
    expect(screen.getByTestId("wf-list-node-10")).toHaveTextContent("Cerrado");
  });

  it("lists transitions from → to with description", () => {
    setup();
    const row = screen.getByTestId("wf-list-transition-1");
    expect(row).toHaveTextContent("Ingresado");
    expect(row).toHaveTextContent("Cerrado");
    expect(within(row).getByRole("textbox")).toHaveValue("cerrar");
  });

  it("adds a status", () => {
    const p = setup();
    fireEvent.change(screen.getByTestId("wf-list-add-status"), { target: { value: "5" } });
    fireEvent.change(screen.getByTestId("wf-list-add-type"), { target: { value: "FINAL" } });
    fireEvent.click(screen.getByTestId("wf-list-add-node"));
    expect(p.onAddNode).toHaveBeenCalledWith(5, "FINAL");
  });

  it("changes a status type", () => {
    const p = setup();
    fireEvent.change(within(screen.getByTestId("wf-list-node-11")).getByRole("combobox"), { target: { value: "FINAL" } });
    expect(p.onChangeType).toHaveBeenCalledWith(11, "FINAL");
  });

  it("adds a transition and rejects a duplicate", () => {
    const p = setup();
    fireEvent.change(screen.getByTestId("wf-list-add-from"), { target: { value: "10" } });
    fireEvent.change(screen.getByTestId("wf-list-add-to"), { target: { value: "12" } });
    fireEvent.click(screen.getByTestId("wf-list-add-transition"));
    expect(p.onAddTransition).not.toHaveBeenCalled();
    expect(screen.getByTestId("wf-list-add-error")).toHaveTextContent("duplicate");
    fireEvent.change(screen.getByTestId("wf-list-add-to"), { target: { value: "11" } });
    fireEvent.change(screen.getByTestId("wf-list-add-desc"), { target: { value: "seguir" } });
    fireEvent.click(screen.getByTestId("wf-list-add-transition"));
    expect(p.onAddTransition).toHaveBeenCalledWith(10, 11, "seguir");
  });

  it("saves a description", () => {
    const p = setup();
    const row = screen.getByTestId("wf-list-transition-1");
    fireEvent.change(within(row).getByRole("textbox"), { target: { value: "finalizar" } });
    fireEvent.click(within(row).getByTestId("wf-list-save-desc-1"));
    expect(p.onSaveDescription).toHaveBeenCalledWith(1, "finalizar");
  });

  it("confirms before deleting a status or a transition", () => {
    const p = setup();
    fireEvent.click(screen.getByTestId("wf-list-delete-node-12"));
    expect(p.onDeleteNode).not.toHaveBeenCalled();
    fireEvent.click(screen.getByTestId("confirm-dialog-confirm"));
    expect(p.onDeleteNode).toHaveBeenCalledWith(12);
    fireEvent.click(screen.getByTestId("wf-list-delete-transition-1"));
    fireEvent.click(screen.getByTestId("confirm-dialog-confirm"));
    expect(p.onDeleteTransition).toHaveBeenCalledWith(1);
  });

  it("names every control for assistive technology", () => {
    setup();
    for (const el of screen.getAllByRole("combobox")) expect(el).toHaveAccessibleName();
    for (const el of screen.getAllByRole("textbox")) expect(el).toHaveAccessibleName();
    for (const el of screen.getAllByRole("button")) expect(el).toHaveAccessibleName();
  });

  it("is read-only outside edit mode", () => {
    setup({ editMode: false });
    expect(screen.queryByTestId("wf-list-add-node")).not.toBeInTheDocument();
    expect(screen.queryByTestId("wf-list-delete-node-12")).not.toBeInTheDocument();
    expect(within(screen.getByTestId("wf-list-node-11")).getByRole("combobox")).toBeDisabled();
  });
});
