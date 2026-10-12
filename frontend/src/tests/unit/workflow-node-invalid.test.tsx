/**
 * #1577: a node with a validation problem is marked on the canvas, not by color alone.
 */
import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { ReactFlowProvider } from "@xyflow/react";

vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string, vals?: Record<string, unknown>) =>
    vals ? `${ns}.${key}:${Object.values(vals).join("|")}` : `${ns}.${key}`,
}));

import { WorkflowNodeBox } from "@/components/workflow/WorkflowNodeBox";

function box(problem?: string) {
  return render(
    <ReactFlowProvider>
      <WorkflowNodeBox
        {...({ id: "4", data: { label: "Aislado", type: "INTERMEDIATE", problem }, selected: false } as unknown as Parameters<typeof WorkflowNodeBox>[0])}
      />
    </ReactFlowProvider>,
  );
}

describe("WorkflowNodeBox validation mark (#1577)", () => {
  it("is not marked when valid", () => {
    box();
    expect(screen.getByTestId("workflow-node-4")).toHaveAttribute("data-invalid", "false");
    expect(screen.queryByTestId("workflow-node-problem-4")).not.toBeInTheDocument();
  });

  it("shows a text mark and adds the problem to its accessible name", () => {
    box("unreachable");
    const node = screen.getByTestId("workflow-node-4");
    expect(node).toHaveAttribute("data-invalid", "true");
    expect(screen.getByTestId("workflow-node-problem-4")).toHaveTextContent("workflowViewer.problem.unreachable");
    expect(node.getAttribute("aria-label")).toContain("workflowViewer.problem.unreachable");
  });
});

describe("WorkflowNodeBox outgoing count (#1578)", () => {
  it("adds the number of outgoing transitions to the accessible name", () => {
    render(
      <ReactFlowProvider>
        <WorkflowNodeBox
          {...({ id: "5", data: { label: "Ingresado", type: "INITIAL", outgoing: 2 }, selected: false } as unknown as Parameters<typeof WorkflowNodeBox>[0])}
        />
      </ReactFlowProvider>,
    );
    expect(screen.getByTestId("workflow-node-5").getAttribute("aria-label")).toContain("workflowViewer.outgoing:2");
  });
});
