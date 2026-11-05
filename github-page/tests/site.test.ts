import { existsSync } from "node:fs";
import path from "node:path";
import { describe, expect, it } from "vitest";
import {
  DOCS_GROUPS,
  DOCS_PAGES,
  FACTORY_ACTS,
  FACTORY_PAGES,
  PRIMARY_NAV,
  PRODUCT_PAGES,
  SECURITY_PAGES,
  breadcrumbs,
  pageMetadata,
  prevNext,
  search,
  sectionOf,
} from "@/lib/site";

const pageFile = (href: string) => path.join(__dirname, "..", "app", href.split("#")[0], "page.tsx");

describe("site map (#1970)", () => {
  it("has the primary entries, including Security", () => {
    expect(PRIMARY_NAV.map((n) => n.label)).toEqual(["AI Factory", "Product", "Security", "Technical docs", "Releases"]);
  });

  it("points every factory, docs, security and top-level href at a real page", () => {
    for (const p of [...FACTORY_PAGES, ...DOCS_PAGES, ...PRODUCT_PAGES, ...SECURITY_PAGES, ...PRIMARY_NAV]) {
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
    expect(sectionOf("/")).toBe("home");
    expect(sectionOf("/product/")).toBe("product");
    expect(sectionOf("/product/story")).toBe("product");
    expect(sectionOf("/ai-sdlc/lessons")).toBe("factory");
    expect(sectionOf("/security/threats/")).toBe("security");
    expect(sectionOf("/docs/security/")).toBe("docs");
    expect(sectionOf("/docs/testing/")).toBe("docs");
    expect(sectionOf("/docs/releases/")).toBe("releases");
  });

  it("links each factory page to its neighbours in reading order", () => {
    expect(prevNext("/ai-sdlc/").prev).toBeUndefined();
    expect(prevNext("/ai-sdlc/").next?.href).toBe("/ai-sdlc/case-study/");
    expect(prevNext("/ai-sdlc/playbook/").next).toBeUndefined();
    expect(prevNext("/docs/testing/").prev?.href).toBe("/docs/api/");
  });

  it("builds breadcrumbs for docs and factory pages", () => {
    expect(breadcrumbs("/docs/testing/").map((c) => c.label)).toEqual(["Home", "Technical docs", "Testing"]);
    expect(breadcrumbs("/ai-sdlc/").map((c) => c.label)).toEqual(["Home", "AI Factory"]);
    expect(breadcrumbs("/")).toEqual([]);
    expect(breadcrumbs("/product/story/").map((c) => c.label)).toEqual(["Home", "Product", "Story"]);
  });

  it("finds pages by title, summary or keyword, ignoring case", () => {
    expect(search("GATES").map((r) => r.href)).toContain("/ai-sdlc/quality-gates/");
    expect(search("stride").map((r) => r.href)).toContain("/security/threats/");
    expect(search("playwright").map((r) => r.href)).toContain("/docs/testing/");
    expect(search("adr")[0].href).toBe("/docs/architecture/");
    expect(search("flyway")[0].href).toBe("/docs/database/");
    expect(search("bruno")[0].href).toBe("/docs/api/");
    expect(search("hexagonal")[0].href).toBe("/docs/hexagonal/");
    expect(search("")).toEqual([]);
    expect(search("zzzz-nothing")).toEqual([]);
  });

  it("gives Product four pages in one sidebar, like the other sections (#1976)", () => {
    expect(PRIMARY_NAV.find((n) => n.key === "product")?.href).toBe("/product/");
    expect(PRODUCT_PAGES.map((p) => p.href)).toEqual(["/product/", "/product/story/", "/product/platform/", "/product/numbers/"]);
    expect(prevNext("/product/story/").prev?.href).toBe("/product/");
    expect(prevNext("/product/story/").next?.href).toBe("/product/platform/");
    expect(pageMetadata("/product/numbers/").title).toBe("Numbers — Product");
    expect(pageMetadata("/security/").title).toBe("Security");
    expect(SECURITY_PAGES.map((p) => p.href)).toEqual([
      "/security/",
      "/security/architecture/",
      "/security/threats/",
      "/security/toolchain/",
      "/security/posture/",
    ]);
    expect(prevNext("/security/threats/").prev?.href).toBe("/security/architecture/");
    expect(breadcrumbs("/security/toolchain/").map((c) => c.label)).toEqual(["Home", "Security", "Toolchain"]);
  });

  it("groups the architecture internals in the Technical docs (#1976)", () => {
    const internals = DOCS_GROUPS.find((g) => g.label === "Architecture internals");
    expect(internals?.pages.map((p) => p.href)).toEqual(["/docs/hexagonal/", "/docs/database/", "/docs/api/"]);
    expect(DOCS_PAGES.map((p) => p.href)).toContain("/docs/stack/");
  });
});
