import { describe, it, expect, vi, afterEach } from "vitest";
import { render, screen, within, fireEvent, cleanup, waitFor } from "@testing-library/react";
import { NextIntlClientProvider } from "next-intl";
import es from "../../../messages/es.json";
import type { ManagementWorkflowTrace } from "@/types";

const motion = vi.hoisted(() => ({ reduced: false }));

vi.mock("motion/react", async (orig) => {
  const actual = await orig<typeof import("motion/react")>();
  return { ...actual, useReducedMotion: () => motion.reduced };
});

import WorkflowTracker from "@/components/motion/WorkflowTracker";

const INSCRIPTION_STATUS_ID = 12;

function makeTrace(overrides: Partial<ManagementWorkflowTrace> = {}): ManagementWorkflowTrace {
  return {
    managementId: 1,
    nodes: [
      { id: 1, statusManagementId: 1, statusManagementName: "Inicio", type: "INITIAL" },
      {
        id: 2,
        statusManagementId: INSCRIPTION_STATUS_ID,
        statusManagementName: "Inscripción",
        type: "INTERMEDIATE",
      },
      { id: 3, statusManagementId: 3, statusManagementName: "Archivada", type: "FINAL" },
    ],
    transitions: [
      { id: 10, originNodeId: 1, destinationNodeId: 2, condition: "docs ok" },
      { id: 11, originNodeId: 2, destinationNodeId: 3 },
    ],
    history: [
      { idHistory: 1, statusManagementId: INSCRIPTION_STATUS_ID, date: "2026-01-02", notes: "ingresó" },
      { idHistory: 2, statusManagementId: INSCRIPTION_STATUS_ID, date: "2026-01-03" },
    ],
    nodeStatuses: { 1: "completed", 2: "in_progress", 3: "pending" },
    ...overrides,
  } as ManagementWorkflowTrace;
}

function renderTracker(trace: ManagementWorkflowTrace) {
  return render(
    <NextIntlClientProvider locale="es" messages={es}>
      <WorkflowTracker trace={trace} />
    </NextIntlClientProvider>,
  );
}

afterEach(() => {
  cleanup();
  motion.reduced = false;
});

describe("WorkflowTracker behaviour", () => {
  it("shows the empty message when the workflow has no nodes", () => {
    renderTracker(makeTrace({ nodes: [], transitions: [] }));
    expect(screen.getByText(es.dashboard.workflow.empty)).toBeInTheDocument();
    expect(screen.queryByTestId("workflow-tracker")).toBeNull();
  });

  it("opens the node modal on click and closes it with the close button", async () => {
    renderTracker(makeTrace());
    expect(screen.queryByTestId("workflow-node-modal")).toBeNull();
    fireEvent.click(screen.getByTestId("workflow-node-2"));
    const modal = screen.getByTestId("workflow-node-modal");
    expect(within(modal).getByRole("dialog", { name: "Inscripción" })).toBeInTheDocument();
    fireEvent.click(within(modal).getByRole("button", { name: es.dashboard.workflow.modal.close }));
    await waitFor(() => expect(screen.queryByTestId("workflow-node-modal")).toBeNull());
  });

  it("opens the modal with Enter and Space and ignores other keys", () => {
    renderTracker(makeTrace());
    const node = screen.getByTestId("workflow-node-1");
    fireEvent.keyDown(node, { key: "a" });
    expect(screen.queryByTestId("workflow-node-modal")).toBeNull();
    fireEvent.keyDown(node, { key: "Enter" });
    expect(screen.getByTestId("workflow-node-modal")).toBeInTheDocument();
    cleanup();
    renderTracker(makeTrace());
    fireEvent.keyDown(screen.getByTestId("workflow-node-1"), { key: " " });
    expect(screen.getByTestId("workflow-node-modal")).toBeInTheDocument();
  });

  it("closes the modal with Escape and by clicking the backdrop, not the dialog body", async () => {
    renderTracker(makeTrace());
    fireEvent.click(screen.getByTestId("workflow-node-2"));
    fireEvent.click(screen.getByRole("dialog"));
    fireEvent.keyDown(window, { key: "Tab" });
    await new Promise((resolve) => setTimeout(resolve, 2000));
    expect(screen.getByTestId("workflow-node-modal")).toBeInTheDocument();
    fireEvent.keyDown(window, { key: "Escape" });
    await waitFor(() => expect(screen.queryByTestId("workflow-node-modal")).toBeNull());
    fireEvent.click(screen.getByTestId("workflow-node-2"));
    fireEvent.click(screen.getByTestId("workflow-node-modal"));
    await waitFor(() => expect(screen.queryByTestId("workflow-node-modal")).toBeNull());
  });

  it("lists incoming and outgoing transitions with their conditions and the node history", () => {
    renderTracker(makeTrace());
    fireEvent.click(screen.getByTestId("workflow-node-2"));
    const modal = screen.getByTestId("workflow-node-modal");
    expect(within(modal).getByText("← Inicio · docs ok")).toBeInTheDocument();
    expect(within(modal).getByText("→ Archivada")).toBeInTheDocument();
    expect(within(modal).getByText("ingresó")).toBeInTheDocument();
    expect(within(modal).queryByText(es.dashboard.workflow.modal.noHistory)).toBeNull();
  });

  it("shows the no-history message and omits empty transition sections", () => {
    renderTracker(makeTrace());
    fireEvent.click(screen.getByTestId("workflow-node-1"));
    const modal = screen.getByTestId("workflow-node-modal");
    expect(within(modal).getByText(es.dashboard.workflow.modal.noHistory)).toBeInTheDocument();
    expect(within(modal).queryByText(es.dashboard.workflow.modal.incoming)).toBeNull();
    expect(within(modal).getByText(es.dashboard.workflow.modal.outgoing)).toBeInTheDocument();
  });

  it("falls back to the node id when a transition references an unknown node", () => {
    renderTracker(
      makeTrace({
        transitions: [
          { id: 10, originNodeId: 1, destinationNodeId: 2 },
          { id: 12, originNodeId: 99, destinationNodeId: 2 },
        ],
      }),
    );
    fireEvent.click(screen.getByTestId("workflow-node-2"));
    expect(within(screen.getByTestId("workflow-node-modal")).getByText("← #99")).toBeInTheDocument();
  });

  it("shows the re-entry badge and movement timeline only on the inscription node", () => {
    renderTracker(
      makeTrace({
        testimonyMovements: [
          { dateEntry: "2026-02-01", dateExit: "2026-02-05", returnedObserved: true },
          { dateEntry: "2026-03-01", returnedObserved: false },
        ],
      }),
    );
    expect(screen.getAllByTestId("workflow-reentry-badge")).toHaveLength(1);
    fireEvent.click(screen.getByTestId("workflow-node-1"));
    expect(screen.queryByTestId("workflow-movement-timeline")).toBeNull();
    fireEvent.keyDown(window, { key: "Escape" });
    fireEvent.click(screen.getByTestId("workflow-node-2"));
    const timeline = screen.getByTestId("workflow-movement-timeline");
    const entries = within(timeline).getAllByTestId("workflow-movement-entry");
    expect(entries).toHaveLength(2);
    expect(within(entries[0]).getByText(es.dashboard.workflow.reentry.returnedObserved)).toBeInTheDocument();
    expect(within(entries[1]).queryByText(es.dashboard.workflow.reentry.returnedObserved)).toBeNull();
    expect(entries[1].textContent).toContain(es.dashboard.workflow.reentry.open);
  });

  it("omits the re-entry badge when no movement was returned", () => {
    renderTracker(makeTrace({ testimonyMovements: [{ dateEntry: "2026-02-01", returnedObserved: false }] }));
    expect(screen.queryByTestId("workflow-reentry-badge")).toBeNull();
    fireEvent.click(screen.getByTestId("workflow-node-2"));
    expect(screen.queryByTestId("workflow-reentry-badge")).toBeNull();
    expect(screen.getByTestId("workflow-movement-timeline")).toBeInTheDocument();
  });

  it("draws a focus ring only on the focused step and removes it on blur", () => {
    const { container } = renderTracker(makeTrace());
    const node = screen.getByTestId("workflow-node-1");
    const before = container.querySelectorAll("rect").length;
    fireEvent.focus(node);
    expect(container.querySelectorAll("rect").length).toBe(before + 1);
    fireEvent.blur(node);
    expect(container.querySelectorAll("rect").length).toBe(before);
  });

  it("skips the traveling dot and the pulse under reduced motion", () => {
    motion.reduced = true;
    const { container } = renderTracker(makeTrace());
    expect(container.querySelectorAll("animateMotion")).toHaveLength(0);
    cleanup();
    motion.reduced = false;
    const animated = renderTracker(makeTrace());
    expect(animated.container.querySelectorAll("animateMotion").length).toBeGreaterThan(0);
  });

  it("treats unknown statuses as pending and draws inactive edges with the inactive arrow", () => {
    const { container } = renderTracker(makeTrace({ nodeStatuses: { 1: "weird" } }));
    expect(screen.getByTestId("workflow-node-1")).toHaveAccessibleName(/Pendiente/);
    expect(container.querySelectorAll("animateMotion")).toHaveLength(0);
    expect(container.querySelector('path[marker-end="url(#wf-arrow)"]')).not.toBeNull();
    expect(container.querySelector('path[marker-end="url(#wf-arrow-active)"]')).toBeNull();
  });

  it("ignores transitions with missing endpoints and unknown nodes", () => {
    const { container } = renderTracker(
      makeTrace({
        transitions: [
          { id: 10, originNodeId: 1 },
          { id: 11, destinationNodeId: 2 },
          { id: 12, originNodeId: 1, destinationNodeId: 77 },
        ],
      }),
    );
    expect(container.querySelectorAll("path[marker-end]")).toHaveLength(0);
  });

  it("places a fork's re-converging node below every branch", () => {
    renderTracker(
      makeTrace({
        nodes: [
          { id: 1, statusManagementName: "A", type: "INITIAL" },
          { id: 2, statusManagementName: "B" },
          { id: 3, statusManagementName: "C" },
          { id: 4, statusManagementName: "D", type: "FINAL" },
        ],
        transitions: [
          { id: 1, originNodeId: 1, destinationNodeId: 2 },
          { id: 2, originNodeId: 1, destinationNodeId: 3 },
          { id: 3, originNodeId: 2, destinationNodeId: 4 },
          { id: 4, originNodeId: 3, destinationNodeId: 4 },
        ],
        nodeStatuses: {},
      }),
    );
    const rectY = (id: number) =>
      Number(screen.getByTestId(`workflow-node-${id}`).querySelector("rect[rx]")!.getAttribute("y"));
    expect(rectY(2)).toBe(rectY(3));
    expect(rectY(4)).toBeGreaterThan(rectY(2));
    expect(rectY(2)).toBeGreaterThan(rectY(1));
  });
});
