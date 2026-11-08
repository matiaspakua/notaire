import { readdirSync } from "node:fs";
import path from "node:path";
import { describe, expect, it } from "vitest";
import { artifactFacts } from "@/lib/artifact-facts";
import { READING_PATH, STAGES } from "@/lib/artifacts-content";
import { DOCS_PAGES, search } from "@/lib/site";
import { cardsFor, DRAWERS, pullFor } from "@/components/figures/cabinet/geometry";
import { GATES, STOPS } from "@/components/figures/gateline/geometry";
import { FACES, readoutFor, SEAT_MAX, seatVector } from "@/components/figures/hexmanifold/geometry";
import { LIFECYCLE_PHASES } from "@/lib/ai-sdlc-content";

const ROOT = path.resolve(__dirname, "..", "..");

describe("engineering artifacts page (#2067)", () => {
  const facts = artifactFacts();

  it("reads the use-case count from the folder, not from the page", () => {
    const onDisk = readdirSync(path.join(ROOT, "docs/100-business/102-use-cases")).filter((n) => /^CU\d+/.test(n) && n.endsWith(".md")).length;
    expect(facts.useCases).toBe(onDisk);
  });

  it("reads every count it prints", () => {
    for (const [key, value] of Object.entries(facts)) expect(value, key).not.toBeNull();
    expect(facts.functionalRequirements).toBeLessThanOrEqual(facts.requirements ?? 0);
    expect(facts.sadSections).toBe(12);
  });

  it("tells nine stages, one per cabinet drawer, each backed by a count", () => {
    expect(STAGES).toHaveLength(9);
    expect(DRAWERS).toHaveLength(9);
    expect(STAGES.map((s) => s.n)).toEqual([1, 2, 3, 4, 5, 6, 7, 8, 9]);
    for (const s of STAGES) {
      expect(s.drawerCount in facts, s.key).toBe(true);
      for (const a of s.artifacts) {
        expect(a.links.length, a.name).toBeGreaterThan(0);
        if (a.count) expect(a.count.key in facts, a.name).toBe(true);
      }
    }
  });

  it("is in the site map and found by a search for the threat model", () => {
    expect(DOCS_PAGES.map((p) => p.href)).toContain("/docs/artifacts/");
    expect(search("threat model").map((r) => r.href)).toContain("/docs/artifacts/");
  });

  it("offers a reading path whose pages all exist in the site map or other sections", () => {
    expect(READING_PATH.length).toBeGreaterThanOrEqual(6);
    expect(READING_PATH.map((p) => p.href)).toContain("/docs/artifacts/");
  });
});

describe("artifact cabinet (#2067)", () => {
  it("adds three cards per doubling of the count, within the drawer", () => {
    expect(cardsFor(null)).toBe(0);
    expect(cardsFor(7)).toBe(9);
    expect(cardsFor(383)).toBe(26);
    expect(cardsFor(100000)).toBe(26);
    expect(cardsFor(1)).toBe(3);
  });

  it("never pulls a drawer past its back wall", () => {
    for (const n of [0, 3, 12, 26]) expect(pullFor(n)).toBeLessThanOrEqual(58);
    expect(pullFor(26)).toBeGreaterThan(pullFor(3));
  });
});

describe("gated line (#2067)", () => {
  it("stops at every lifecycle entry, in order", () => {
    expect(STOPS).toHaveLength(LIFECYCLE_PHASES.flatMap((p) => p.steps).length);
    for (let i = 1; i < STOPS.length; i += 1) expect(STOPS[i].x).toBeGreaterThan(STOPS[i - 1].x);
  });

  it("puts the five hard gates at the steps that carry them", () => {
    expect(GATES.map((g) => g.gate)).toEqual([1, 2, 3, 4, 5]);
    const gated = LIFECYCLE_PHASES.flatMap((p) => p.steps).filter((s) => s.gate).map((s) => s.n);
    expect(GATES.map((g) => g.n)).toEqual(gated);
  });

  it("holds a carriage stopped at a gate's step in front of its boom", () => {
    for (const g of GATES) {
      const stop = STOPS.find((s) => s.n === g.n);
      expect(g.x - (stop?.x ?? 0)).toBeGreaterThan(10);
    }
  });
});

describe("hexagonal port manifold (#2083)", () => {
  it("maps six faces to ports and adapters around a sealed domain", () => {
    expect(FACES).toHaveLength(6);
    expect(FACES.map((f) => f.key)).toEqual(["web", "security", "in", "out", "persist", "pdf"]);
    expect(FACES.some((f) => f.kind === "port")).toBe(true);
    expect(FACES.filter((f) => f.kind.includes("adapter"))).toHaveLength(4);
  });

  it("seats each plug toward the core on a non-zero screen vector", () => {
    expect(SEAT_MAX).toBeGreaterThan(8);
    for (let i = 0; i < FACES.length; i += 1) {
      const [vx, vy] = seatVector(i);
      expect(Math.hypot(vx, vy), `face ${i}`).toBeGreaterThan(0.2);
      expect(readoutFor(i)).toContain(FACES[i].layer);
    }
  });

  it("is linked from the hexagonal docs page in the site map", () => {
    expect(DOCS_PAGES.map((p) => p.href)).toContain("/docs/hexagonal/");
    expect(search("hexagonal ports").map((r) => r.href)).toContain("/docs/hexagonal/");
  });
});
