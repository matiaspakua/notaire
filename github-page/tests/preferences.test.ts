import { readFileSync } from "node:fs";
import path from "node:path";
import { describe, expect, it } from "vitest";
import { contrastRatio, DARK_PAIRS } from "@/lib/theme-pairs";
import {
  LANG_CYCLE,
  LANG_KEY,
  THEME_CYCLE,
  THEME_KEY,
  nextChoice,
  parseLangChoice,
  parseThemeChoice,
  resolveLang,
  resolveTheme,
} from "@/lib/preferences";
import { PREFERENCE_BOOT } from "@/lib/preference-boot";

describe("theme and language choices (#2201)", () => {
  it("follows the system until the visitor chooses", () => {
    expect(resolveTheme("system", true)).toBe("dark");
    expect(resolveTheme("system", false)).toBe("light");
    expect(resolveTheme("light", true)).toBe("light");
    expect(resolveTheme("dark", false)).toBe("dark");
    expect(resolveLang("system", "es-ES")).toBe("es");
    expect(resolveLang("system", "en-US")).toBe("en");
    expect(resolveLang("en", "es-AR")).toBe("en");
    expect(resolveLang("es", "en")).toBe("es");
  });

  it("cycles light, dark, then back to the system", () => {
    expect(nextChoice(THEME_CYCLE, "light")).toBe("dark");
    expect(nextChoice(THEME_CYCLE, "dark")).toBe("system");
    expect(nextChoice(THEME_CYCLE, "system")).toBe("light");
    expect(nextChoice(LANG_CYCLE, "en")).toBe("es");
    expect(nextChoice(LANG_CYCLE, "es")).toBe("system");
    expect(parseThemeChoice("nope")).toBe("system");
    expect(parseLangChoice(null)).toBe("system");
  });

  it("boots from localStorage before paint and does not track", () => {
    expect(PREFERENCE_BOOT).toContain(THEME_KEY);
    expect(PREFERENCE_BOOT).toContain(LANG_KEY);
    expect(PREFERENCE_BOOT).toContain("prefers-color-scheme: dark");
    expect(PREFERENCE_BOOT).not.toMatch(/google|analytics|gtag|beacon/i);
    const css = readFileSync(path.join(__dirname, "../app/globals.css"), "utf8");
    expect(css).toContain('[data-theme="dark"]');
    expect(css).toContain("prefers-reduced-motion: reduce");
    for (const pair of DARK_PAIRS) {
      expect(css).toContain(pair.fg);
      expect(contrastRatio(pair.fg, pair.bg)).toBeGreaterThanOrEqual(4.5);
    }
  });
});
