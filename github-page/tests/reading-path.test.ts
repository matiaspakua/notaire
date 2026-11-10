import { existsSync } from "node:fs";
import path from "node:path";
import { describe, expect, it } from "vitest";
import { FACTORY_READING_PATH } from "@/lib/reading-path";
import { pageIsTranslated } from "@/lib/i18n";

describe("factory reading order (#2201)", () => {
  it("starts at the overview and ends at the playbook", () => {
    expect(FACTORY_READING_PATH[0].href).toBe("/ai-sdlc/");
    expect(FACTORY_READING_PATH.at(-1)?.href).toBe("/ai-sdlc/playbook/");
    const hrefs = FACTORY_READING_PATH.map((s) => s.href);
    expect(new Set(hrefs).size).toBe(hrefs.length);
    for (const step of FACTORY_READING_PATH) {
      const file = path.join(__dirname, "..", "app", step.href, "page.tsx");
      expect(existsSync(file), step.href).toBe(true);
    }
  });

  it("translates the home page and the factory overview only", () => {
    expect(pageIsTranslated("/")).toBe(true);
    expect(pageIsTranslated("/ai-sdlc/")).toBe(true);
    expect(pageIsTranslated("/ai-sdlc")).toBe(true);
    expect(pageIsTranslated("/docs/hexagonal/")).toBe(false);
    expect(pageIsTranslated("/product/story/")).toBe(false);
  });
});
