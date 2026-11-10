import { act, fireEvent, render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { FlowDiagram } from "@/components/diagrams/FlowDiagram";
import { FlywayTimeline } from "@/components/diagrams/FlywayTimeline";
import { HexagonMap } from "@/components/diagrams/HexagonMap";
import { PipelineMap } from "@/components/diagrams/PipelineMap";
import { TestPyramid } from "@/components/diagrams/TestPyramid";
import { contractFlow, factoryFlow, qualityGateFlow, requestFlow, trustBoundaryFlow } from "@/lib/diagram-flows";

const live = (id: string) => document.getElementById(`${id}-live`) as HTMLElement;

function renderRequestFlow() {
  const f = requestFlow();
  return render(<FlowDiagram id="request" title="One payment request" desc="d" caption="c" {...f} />);
}

describe("interactive diagrams (#1977)", () => {
  it("starts on step one and never plays on its own", () => {
    vi.useFakeTimers();
    try {
      renderRequestFlow();
      expect(live("request")).toHaveTextContent("The UI posts a payment");
      act(() => void vi.advanceTimersByTime(10_000));
      expect(live("request")).toHaveTextContent("The UI posts a payment");
      expect(screen.getByRole("button", { name: /play all steps/i })).toHaveAttribute("aria-pressed", "false");
    } finally {
      vi.useRealTimers();
    }
  });

  it("steps with buttons and with the arrow keys, and announces each step", async () => {
    renderRequestFlow();
    expect(screen.getByRole("button", { name: /previous step/i })).toBeDisabled();
    await userEvent.click(screen.getByRole("button", { name: /next step/i }));
    expect(live("request")).toHaveTextContent("The controller makes a command");
    expect(live("request")).toHaveAttribute("aria-live", "polite");
    fireEvent.keyDown(screen.getByRole("button", { name: /next step/i }), { key: "ArrowRight" });
    expect(live("request")).toHaveTextContent("The in-port is the contract");
    fireEvent.keyDown(screen.getByRole("button", { name: /next step/i }), { key: "ArrowLeft" });
    expect(live("request")).toHaveTextContent("The controller makes a command");
  });

  it("plays through every step once when asked, then stops", () => {
    vi.useFakeTimers();
    try {
      renderRequestFlow();
      fireEvent.click(screen.getByRole("button", { name: /play all steps/i }));
      for (let i = 0; i < 10; i += 1) act(() => void vi.advanceTimersByTime(2500));
      expect(live("request")).toHaveTextContent("The answer goes back the same way");
      expect(screen.getByRole("button", { name: /play all steps/i })).toHaveAttribute("aria-pressed", "false");
    } finally {
      vi.useRealTimers();
    }
  });

  it("lists every step as a button that marks the current one", async () => {
    renderRequestFlow();
    const list = screen.getByRole("list", { name: /all steps/i });
    const items = within(list).getAllByRole("button");
    expect(items).toHaveLength(requestFlow().steps.length);
    await userEvent.click(items[3]);
    expect(items[3]).toHaveAttribute("aria-current", "step");
    expect(live("request")).toHaveTextContent("The use case applies the rules");
  });

  it("gives the SVG an accessible name and description", () => {
    renderRequestFlow();
    const img = screen.getByRole("img", { name: /one payment request/i });
    expect(img.querySelector("desc")).not.toBeNull();
  });

  it("only references nodes and edges that exist", () => {
    for (const f of [requestFlow(), contractFlow({ paths: 1, operations: 2 }, 3), factoryFlow(), trustBoundaryFlow(), qualityGateFlow()]) {
      const ids = new Set(f.nodes.map((n) => n.id));
      for (const e of f.edges) expect(ids.has(e.from) && ids.has(e.to), `${e.from}->${e.to}`).toBe(true);
      for (const s of f.steps) {
        for (const n of s.nodes) expect(ids.has(n), n).toBe(true);
        for (const e of s.edges ?? []) expect(e).toBeLessThan(f.edges.length);
      }
    }
  });

  it("tells the factory story that is actually on main", () => {
    const text = factoryFlow().steps.map((step) => `${step.title}\n${step.body}`).join("\n");
    expect(text).not.toMatch(/only Matias merges/);
    expect(text).toMatch(/Workers never merge/);
    expect(text).toMatch(/Foreman/);
    expect(text).toMatch(/preflight\.sh --full/);
    expect(text).toMatch(/version tag/);
    expect(text).toMatch(/foreman plan/);
    expect(text).toMatch(/example-method/);
    expect(text).toMatch(/not loaded/);
    expect(factoryFlow().steps[0].title).toMatch(/issue/i);
  });

  it("steps the quality gates from the agent machine to the schedule", () => {
    const titles = qualityGateFlow().steps.map((step) => step.title);
    expect(titles[0]).toMatch(/agent machine/i);
    expect(titles.map((title) => title.toLowerCase()).join(" ")).toMatch(/version tag/);
    expect(titles.map((title) => title.toLowerCase()).join(" ")).toMatch(/main/);
    expect(titles.map((title) => title.toLowerCase()).join(" ")).toMatch(/schedule/);
    expect(qualityGateFlow().steps.map((step) => step.body).join(" ")).toMatch(/Workers never merge/);
  });

  it("steps trust from the browser to the database", () => {
    const steps = trustBoundaryFlow().steps;
    expect(steps[0].title).toMatch(/browser/i);
    expect(steps.at(-1)?.nodes).toEqual(expect.arrayContaining(["uc", "db"]));
  });

  it("shows the hexagon counts it is given", () => {
    render(
      <HexagonMap
        facts={{ domainContexts: 9, useCaseContexts: 22, inPorts: 86, outPorts: 58, useCases: 78, webAdapters: 58, outAdapters: 91, baselineViolations: 0 }}
        sources={{ domain: "#", port: "#", usecase: "#", adapter: "#", rules: "#" }}
      />,
    );
    expect(live("hexagon")).toHaveTextContent("9 bounded contexts");
  });

  it("puts each workflow under the triggers in its on: block", () => {
    render(
      <PipelineMap
        repoHref="https://example.test"
        workflows={[
          { file: "ci.yml", name: "CI", triggers: ["pull_request", "push"] },
          { file: "cd.yml", name: "CD", triggers: ["workflow_run"] },
          { file: "zap.yml", name: "ZAP", triggers: ["schedule", "workflow_dispatch"] },
        ]}
      />,
    );
    expect(live("pipeline")).toHaveTextContent("Pull request: 1 workflow");
    const list = screen.getByRole("list", { name: /all steps/i });
    expect(within(list).getAllByRole("button").map((b) => b.textContent)).toEqual([
      "1Pull request: 1 workflow",
      "2Push: 1 workflow",
      "3After CI on main: 1 workflow",
      "4Scheduled: 1 workflow",
    ]);
  });

  it("groups migrations into the phases that have any", () => {
    render(
      <FlywayTimeline
        sourceHref="#"
        migrations={[
          { version: 1, name: "baseline" },
          { version: 2, name: "seed" },
          { version: 30, name: "rename" },
        ]}
        phases={[
          { from: 1, to: 2, title: "Baseline", body: "b" },
          { from: 3, to: 24, title: "Fixes", body: "f" },
          { from: 25, to: 37, title: "English names", body: "e" },
        ]}
      />,
    );
    expect(live("flyway-timeline")).toHaveTextContent("Baseline · V1–V2");
    expect(within(screen.getByRole("list", { name: /all steps/i })).getAllByRole("button")).toHaveLength(2);
  });

  it("labels the test pyramid with the real counts", () => {
    render(
      <TestPyramid
        facts={{ backend: 352, frontend: 197, e2e: 102, bruno: 383 }}
        sources={{ backend: "#", frontend: "#", bruno: "#", e2e: "#", arch: "#" }}
      />,
    );
    expect(screen.getByRole("img", { name: /test pyramid/i })).toHaveTextContent("352 test classes");
  });
});
