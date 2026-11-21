import { existsSync, readFileSync } from "node:fs";
import path from "node:path";
import { describe, expect, it } from "vitest";
import { FACTORY_ACTS, FACTORY_PAGES } from "@/lib/site";
import { FACTORY_READING_PATH } from "@/lib/reading-path";
import { pageIsTranslated } from "@/lib/i18n";
import { TRANSLATED_ROUTES } from "@/lib/i18n/es";
import { loadRepoLensBench, headlineFactors } from "@/lib/repolens-bench";
import { loadRepoLensRelease } from "@/lib/repolens-release";

const REPO = path.join(__dirname, "..", "..");
const RESULTS = path.join(REPO, "framework", "repolens", "bench", "results");

function loadJson(name: string) {
  return JSON.parse(readFileSync(path.join(RESULTS, name), "utf8")) as {
    tasks?: number;
    summary: Record<string, Record<string, number>>;
  };
}

describe("RepoLens Pages showcase (#2315)", () => {
  it("registers /ai-sdlc/repolens/ in Act IV after Results", () => {
    const learned = FACTORY_ACTS[3].pages.map((p) => p.href);
    expect(learned).toContain("/ai-sdlc/results/");
    expect(learned).toContain("/ai-sdlc/repolens/");
    expect(learned.indexOf("/ai-sdlc/repolens/")).toBeGreaterThan(learned.indexOf("/ai-sdlc/results/"));
    expect(FACTORY_PAGES.some((p) => p.href === "/ai-sdlc/repolens/")).toBe(true);
    expect(existsSync(path.join(__dirname, "..", "app", "ai-sdlc", "repolens", "page.tsx"))).toBe(true);
  });

  it("is on the reading path, translated routes, and findable by search keywords", () => {
    expect(FACTORY_READING_PATH.map((s) => s.href)).toContain("/ai-sdlc/repolens/");
    expect(TRANSLATED_ROUTES).toContain("/ai-sdlc/repolens/");
    expect(pageIsTranslated("/ai-sdlc/repolens/")).toBe(true);
    const page = FACTORY_PAGES.find((p) => p.href === "/ai-sdlc/repolens/");
    expect(page?.keywords).toMatch(/repolens|retrieval|benchmark/i);
  });

  it("pins headline factors to the committed retrieval and sonnet JSON (no invented ratios)", () => {
    const tune = loadJson("retrieval.json");
    const hold = loadJson("retrieval-notaire-holdout.json");
    const agent = loadJson("agent-sonnet-holdout-0.3.0.json");
    const snap = loadRepoLensBench();
    const factors = headlineFactors(snap);

    expect(tune.tasks).toBe(11);
    expect(hold.tasks).toBe(12);
    expect(tune.summary.graph["recall@10"] / tune.summary.grep["recall@10"]).toBeCloseTo(4.8956, 3);
    expect(hold.summary.graph["recall@10"] / hold.summary.grep["recall@10"]).toBeCloseTo(2.5, 5);
    expect(tune.summary.grep.latency_ms / tune.summary.graph.latency_ms).toBeCloseTo(42.754, 2);
    expect(tune.summary.grep.output_bytes / tune.summary.graph.output_bytes).toBeCloseTo(90.743, 2);

    expect(factors.tuningRecallLabel).toBe("4.9x more");
    expect(factors.holdoutRecallLabel).toBe("2.5x more");
    expect(factors.latencyLabel).toBe("43x faster");
    expect(factors.bytesLabel).toBe("91x less");
    expect(factors.sonnetToolCallsLabel).toBe("2.0x fewer");
    expect(agent.summary.baseline.tool_calls).toBe(4);
    expect(agent.summary.graph.tool_calls).toBe(2);
    expect(snap.retrieval.tuning.tasks).toBe(11);
    expect(snap.retrieval.holdout.tasks).toBe(12);
    expect(snap.agents.sonnetHoldout.baseline.tool_calls).toBe(4);
    expect(snap.agents.sonnetHoldout.graph.tool_calls).toBe(2);
  });

  it("includes every latest offline agent/retrieval run and factory-loop pairs from committed sources", () => {
    const snap = loadRepoLensBench();
    expect(snap.sources.map((s) => s.path).sort()).toEqual(
      [
        "framework/repolens/bench/results/agent-haiku-notaire-holdout.json",
        "framework/repolens/bench/results/agent-haiku.json",
        "framework/repolens/bench/results/agent-sonnet-holdout-0.3.0.json",
        "framework/repolens/bench/results/agent-sonnet-scale.json",
        "framework/repolens/bench/results/factory-loop-comparison.md",
        "framework/repolens/bench/results/retrieval-notaire-holdout.json",
        "framework/repolens/bench/results/retrieval.json",
      ].sort(),
    );
    expect(snap.agents.haikuTuning.baseline.runs).toBe(33);
    expect(snap.agents.haikuHoldout.baseline.runs).toBe(36);
    expect(snap.agents.sonnetScale.baseline.runs).toBe(8);
    expect(snap.factoryLoop.pairs.length).toBeGreaterThanOrEqual(2);
    expect(snap.factoryLoop.caveat).toMatch(/percentage|n\s*=/i);
  });

  it("reads the package version and the current changelog section", () => {
    const release = loadRepoLensRelease();
    const init = readFileSync(
      path.join(REPO, "framework/repolens/repolens/__init__.py"),
      "utf8",
    );
    const page = readFileSync(path.join(__dirname, "..", "app", "ai-sdlc", "repolens", "page.tsx"), "utf8");
    expect(init).toContain(`__version__ = "${release.version}"`);
    expect(release.version).toMatch(/^\d+\.\d+\.\d+$/);
    expect(release.highlights.length).toBeGreaterThan(0);
    expect(release.highlights.join("\n")).toMatch(/session/);
    expect(page).toContain("loadRepoLensRelease");
    expect(page).toContain("release.version");
  });
});
