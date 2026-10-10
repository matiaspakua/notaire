/**
 * WorkflowTracker accessibility (#1353): the diagram is a named group (not an
 * img with focusable children), every step is a button named with its state,
 * and no animation loops forever (WCAG 2.2.2: stops within 5 seconds).
 */
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { describe, it, expect, vi } from "vitest";
import { render, screen, within } from "@testing-library/react";
import { NextIntlClientProvider } from "next-intl";
import es from "../../../messages/es.json";
import type { GestionWorkflowTrace } from "@/types";

vi.mock("motion/react", async (orig) => {
  const actual = await orig<typeof import("motion/react")>();
  return { ...actual, useReducedMotion: () => false };
});

import WorkflowTracker from "@/components/motion/WorkflowTracker";

const trace: GestionWorkflowTrace = {
  managementId: 1,
  workflowDefinition: { id: 9, name: "Compraventa" } as GestionWorkflowTrace["workflowDefinition"],
  nodes: [
    { id: 1, statusManagementName: "Inicio", type: "INITIAL" },
    { id: 2, statusManagementName: "Documentación completa", type: "INTERMEDIATE" },
    { id: 3, statusManagementName: "Archivada", type: "FINAL" },
  ],
  transitions: [
    { id: 10, originNodeId: 1, destinationNodeId: 2 },
    { id: 11, originNodeId: 2, destinationNodeId: 3 },
  ],
  history: [],
  nodeStatuses: { 1: "completed", 2: "in_progress", 3: "pending" },
};

function renderTracker() {
  return render(
    <NextIntlClientProvider locale="es" messages={es}>
      <WorkflowTracker trace={trace} />
    </NextIntlClientProvider>,
  );
}

describe("WorkflowTracker accessibility (#1353)", () => {
  it("is a named group with a translated role description, not an img", () => {
    const { container } = renderTracker();
    expect(container.querySelector('svg[role="img"]')).toBeNull();
    const group = screen.getByRole("group", { name: "Compraventa" });
    expect(group).toHaveAttribute("aria-roledescription", "diagrama de flujo");
  });

  it("exposes every step as a button named with its state", () => {
    renderTracker();
    const group = screen.getByRole("group", { name: "Compraventa" });
    const steps = within(group).getAllByRole("button");
    expect(steps).toHaveLength(3);
    expect(steps[0]).toHaveAccessibleName(/Inicio — Completada/);
    expect(steps[1]).toHaveAccessibleName(/Documentación completa — En curso/);
    expect(steps[2]).toHaveAccessibleName(/Archivada — Pendiente/);
  });

  it("the traveling dot on the active edge does not repeat indefinitely", () => {
    const { container } = renderTracker();
    const dots = container.querySelectorAll("animateMotion");
    expect(dots.length).toBeGreaterThan(0);
    for (const dot of dots) {
      expect(dot.getAttribute("repeatCount")).not.toBe("indefinite");
      const total = parseFloat(dot.getAttribute("dur")!) * Number(dot.getAttribute("repeatCount"));
      expect(total).toBeLessThanOrEqual(5);
    }
  });

  it("the in-progress pulse is bounded (no repeat: Infinity in the tracker)", () => {
    const source = readFileSync(join(__dirname, "../../components/motion/WorkflowTracker.tsx"), "utf8");
    expect(source).not.toMatch(/repeat:\s*Infinity/);
    expect(source).not.toMatch(/repeatCount="indefinite"/);
  });
});
