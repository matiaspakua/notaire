import { existsSync } from "node:fs";
import path from "node:path";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it } from "vitest";
import { AnatomyFigure } from "@/components/anatomy/AnatomyFigure";
import { engineeringFigure } from "@/components/anatomy/engineering-figure";
import type { FigureData } from "@/components/anatomy/figure-types";
import { sdlcFigure } from "@/components/anatomy/sdlc-figure";
import { systemFigure } from "@/components/anatomy/system-figure";
import { DOCS_PAGES, FACTORY_PAGES, PRODUCT_PAGES, search } from "@/lib/site";

const ROOT = path.resolve(__dirname, "..", "..");
const FIGURES: FigureData[] = [systemFigure, engineeringFigure, sdlcFigure];

/** Repository paths named in a `where` line: every token that contains a slash, minus parentheticals. */
function pathsIn(where: string): string[] {
  return where
    .replace(/\([^)]*\)/g, "")
    .split(/[;,]/)
    .map((t) => t.trim().split(/\s+/)[0])
    .filter((t) => t.includes("/"));
}

describe("Anatomy figures (#2089)", () => {
  it.each(FIGURES.map((f) => [f.id, f] as const))("%s is a dense machine, not a diagram", (_id, fig) => {
    expect(fig.svg.match(/class="iso-solid"/g)?.length ?? 0).toBeGreaterThanOrEqual(60);
    expect(fig.steps.length).toBeGreaterThanOrEqual(6);
  });

  it.each(FIGURES.map((f) => [f.id, f] as const))("%s: every step lights a station that exists and moves parts that exist", (_id, fig) => {
    for (const step of fig.steps) {
      expect(fig.svg, `${fig.id} station ${step.station}`).toContain(`data-station="${step.station}"`);
      expect(step.title.length).toBeGreaterThan(3);
      expect(step.text.length).toBeGreaterThan(60);
      expect(step.readout.length).toBeGreaterThan(3);
      for (const part of Object.keys(step.pose)) expect(fig.svg, `${fig.id} part ${part}`).toContain(`data-part="${part}"`);
    }
  });

  it.each(FIGURES.map((f) => [f.id, f] as const))("%s: every path a step points at exists in the repository", (_id, fig) => {
    const missing = fig.steps.flatMap((s) => pathsIn(s.where)).filter((p) => !existsSync(path.join(ROOT, p)));
    expect(missing).toEqual([]);
  });

  it.each(FIGURES.map((f) => [f.id, f] as const))("%s: no step claims that GitHub re-runs the product suite on a pull request", (_id, fig) => {
    const text = fig.steps.map((s) => s.text).join(" ");
    expect(text).not.toMatch(/CI is red|light CI/i);
  });

  it("steps through a figure with Next and Previous and writes the readout", async () => {
    render(<AnatomyFigure data={systemFigure} />);
    const readout = () => document.querySelector("[data-readout]")?.textContent;
    expect(readout()).toBe(systemFigure.steps[0].readout);
    await userEvent.click(screen.getByRole("button", { name: /next step/i }));
    expect(readout()).toBe(systemFigure.steps[1].readout);
    expect(document.querySelector('[data-station="1"]')).toHaveAttribute("data-dim");
    expect(document.querySelector('[data-station="2"]')).not.toHaveAttribute("data-dim");
    await userEvent.click(screen.getByRole("button", { name: /previous step/i }));
    expect(readout()).toBe(systemFigure.steps[0].readout);
  });

  it("moves the ticket and opens a gate when a step asks for it", async () => {
    render(<AnatomyFigure data={sdlcFigure} />);
    const boat = () => document.querySelector<SVGElement>('[data-part="boat"]')!.style.transform;
    const first = boat();
    await userEvent.click(screen.getByRole("button", { name: /next step/i }));
    expect(boat()).not.toBe(first);
    expect(document.querySelector<SVGElement>('[data-part="gate-1"]')!.style.transform).toBe(sdlcFigure.steps[1].pose["gate-1"].transform);
  });

  it("does not play by itself", async () => {
    render(<AnatomyFigure data={engineeringFigure} />);
    expect(screen.getByRole("button", { name: /play all steps/i })).toHaveAttribute("aria-pressed", "false");
  });
});

describe("Anatomy pages in the site map (#2089)", () => {
  it("registers the four new pages and a glossary entry", () => {
    expect(PRODUCT_PAGES.map((p) => p.href)).toContain("/product/anatomy/");
    expect(DOCS_PAGES.map((p) => p.href)).toEqual(expect.arrayContaining(["/docs/engineering/", "/docs/glossary/"]));
    expect(FACTORY_PAGES.map((p) => p.href)).toContain("/ai-sdlc/anatomy/");
  });

  it("finds the anatomy pages by the words a reader would type", () => {
    expect(search("anatomy").map((r) => r.href)).toEqual(expect.arrayContaining(["/product/anatomy/", "/docs/engineering/", "/ai-sdlc/anatomy/"]));
    expect(search("glossary")[0].href).toBe("/docs/glossary/");
    expect(search("environments").map((r) => r.href)).toContain("/docs/engineering/");
  });

  it("has no duplicate hrefs", () => {
    const all = [...FACTORY_PAGES, ...DOCS_PAGES, ...PRODUCT_PAGES].map((p) => p.href);
    expect(new Set(all).size).toBe(all.length);
  });
});
