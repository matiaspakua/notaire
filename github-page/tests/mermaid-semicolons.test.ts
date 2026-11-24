import { describe, expect, it } from "vitest";

import * as content from "@/lib/ai-sdlc-content";

/** #2475: Mermaid reads `;` as a statement break, so a message with one broke the governance diagram ("Parse error"). */
describe("Mermaid charts carry no statement-breaking semicolons", () => {
  const charts = Object.entries(content).filter(
    ([, value]) => typeof value === "string" && /^\s*(sequenceDiagram|flowchart|graph|stateDiagram)/.test(value),
  ) as [string, string][];

  it("finds the charts", () => expect(charts.length).toBeGreaterThan(0));

  for (const [name, chart] of charts) {
    it(`${name} has no ';' inside a message or label`, () => {
      const bad = chart.split("\n").filter((line) => /:.*;|\[[^\]]*;[^\]]*\]/.test(line) && !/^\s*%%/.test(line));
      expect(bad).toEqual([]);
    });
  }
});
