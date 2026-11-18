import { describe, expect, it } from "vitest";
import { existsSync, readFileSync } from "node:fs";
import path from "node:path";
import { repolensPipelineFlow } from "@/lib/diagram-flows";
import { loadRepoLensBench, headlineFactors } from "@/lib/repolens-bench";

describe("RepoLens UI polish (#2318)", () => {
  it("exports a ranking pipeline with five stages and step controls data", () => {
    const flow = repolensPipelineFlow();
    expect(flow.nodes.map((n) => n.id)).toEqual(["parse", "store", "embed", "rank", "serve"]);
    expect(flow.steps.length).toBeGreaterThanOrEqual(5);
    expect(flow.edges.length).toBeGreaterThanOrEqual(4);
    expect(flow.width).toBeGreaterThan(600);
  });

  it("keeps metric honesty after polish and ships the polished page modules", () => {
    const snap = loadRepoLensBench();
    const factors = headlineFactors(snap);
    expect(factors.tuningRecallLabel).toBe("4.9x more");
    expect(factors.bytesLabel).toBe("91x less");
    const charts = path.join(__dirname, "..", "components", "factory", "RepoLensCharts.tsx");
    const page = path.join(__dirname, "..", "app", "ai-sdlc", "repolens", "page.tsx");
    expect(existsSync(charts)).toBe(true);
    expect(existsSync(page)).toBe(true);
    const chartsSrc = readFileSync(charts, "utf8");
    expect(chartsSrc).toMatch(/tooltip|aria-describedby|onFocus|title=/i);
    expect(chartsSrc).toMatch(/framer-motion|motion\./);
    const pageSrc = readFileSync(page, "utf8");
    expect(pageSrc).toMatch(/repolensPipelineFlow|FlowDiagram/);
  });
});
