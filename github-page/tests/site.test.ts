import { existsSync } from "node:fs";
import path from "node:path";
import { describe, expect, it } from "vitest";
import {
  DOCS_PAGES,
  FACTORY_ACTS,
  FACTORY_PAGES,
  PRIMARY_NAV,
  breadcrumbs,
  prevNext,
  search,
  sectionOf,
} from "@/lib/site";

const pageFile = (href: string) => path.join(__dirname, "..", "app", href.split("#")[0], "page.tsx");

describe("site map (#1970)", () => {
  it("has the four top-level entries the Owner asked for", () => {
    expect(PRIMARY_NAV.map((n) => n.label)).toEqual(["AI Factory", "Product", "Technical docs", "Releases"]);
  });

  it("points every factory, docs and top-level href at a real page", () => {
    for (const p of [...FACTORY_PAGES, ...DOCS_PAGES, ...PRIMARY_NAV]) {
      expect(existsSync(pageFile(p.href)), p.href).toBe(true);
    }
  });

  it("tells the factory as five acts, problem first and adoption last", () => {
    expect(FACTORY_ACTS).toHaveLength(5);
    expect(FACTORY_ACTS[0].pages[0].href).toBe("/ai-sdlc/");
    expect(FACTORY_ACTS[3].pages.map((p) => p.href)).toContain("/ai-sdlc/results/");
    expect(FACTORY_ACTS[4].pages.map((p) => p.href)).toContain("/ai-sdlc/playbook/");
  });

  it("maps paths to their section", () => {
    expect(sectionOf("/")).toBe("product");
    expect(sectionOf("/ai-sdlc/lessons")).toBe("factory");
    expect(sectionOf("/docs/testing/")).toBe("docs");
    expect(sectionOf("/docs/releases/")).toBe("releases");
  });

  it("links each factory page to its neighbours in reading order", () => {
    expect(prevNext("/ai-sdlc/").prev).toBeUndefined();
    expect(prevNext("/ai-sdlc/").next?.href).toBe("/ai-sdlc/case-study/");
    expect(prevNext("/ai-sdlc/playbook/").next).toBeUndefined();
    expect(prevNext("/docs/testing/").prev?.href).toBe("/docs/architecture/");
  });

  it("builds breadcrumbs for docs and factory pages", () => {
    expect(breadcrumbs("/docs/testing/").map((c) => c.label)).toEqual(["Home", "Technical docs", "Testing"]);
    expect(breadcrumbs("/ai-sdlc/").map((c) => c.label)).toEqual(["Home", "AI Factory"]);
    expect(breadcrumbs("/")).toEqual([]);
  });

  it("finds pages by title, summary or keyword, ignoring case", () => {
    expect(search("GATES").map((r) => r.href)).toContain("/ai-sdlc/quality-gates/");
    expect(search("playwright").map((r) => r.href)).toContain("/docs/testing/");
    expect(search("adr")[0].href).toBe("/docs/architecture/");
    expect(search("")).toEqual([]);
    expect(search("zzzz-nothing")).toEqual([]);
  });
});
