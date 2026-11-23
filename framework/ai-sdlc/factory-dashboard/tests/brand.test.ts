import { readFileSync } from "node:fs";

import { describe, expect, it } from "vitest";

import { translator } from "../src/ui/i18n.ts";

const BRAND = "AI SDLC Factory";

describe("dashboard brand (#2457)", () => {
  it("names itself AI SDLC Factory in every locale", () => {
    expect(translator("en")("app.name")).toBe(BRAND);
    expect(translator("es")("app.name")).toBe(BRAND);
  });

  it("uses the brand as the page title", () => {
    const html = readFileSync(new URL("../src/ui/index.html", import.meta.url), "utf8");
    expect(html).toContain(`<title>${BRAND}</title>`);
  });
});
