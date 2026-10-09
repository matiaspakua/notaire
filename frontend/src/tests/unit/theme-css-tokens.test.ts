/**
 * Tailwind v4 semantic colour utilities (#1337, RNF-05 / RNF-09).
 *
 * Tailwind v4 only generates `bg-primary`, `text-muted-foreground`,
 * `text-destructive`... for colours registered as `--color-*` in an `@theme`
 * block. globals.css declares the shadcn HSL triplets in `:root`; without the
 * mapping every semantic class compiles to nothing (white primary buttons, no
 * active nav pill, black delete icons).
 */
import { describe, it, expect } from "vitest";
import { readFileSync } from "node:fs";
import { resolve } from "node:path";

const css = readFileSync(resolve(__dirname, "../../app/globals.css"), "utf8");

const TOKENS = [
  "background",
  "foreground",
  "card",
  "card-foreground",
  "popover",
  "popover-foreground",
  "primary",
  "primary-foreground",
  "secondary",
  "secondary-foreground",
  "muted",
  "muted-foreground",
  "accent",
  "accent-foreground",
  "destructive",
  "destructive-foreground",
  "border",
  "input",
  "ring",
];

function themeBlock(): string {
  const match = css.match(/@theme\s+inline\s*\{([^}]*)\}/);
  return match ? match[1] : "";
}

function rootVar(name: string): string | undefined {
  const root = css.match(/:root\s*\{([^}]*)\}/)?.[1] ?? "";
  return root.match(new RegExp(`--${name}:\\s*([^;]+);`))?.[1].trim();
}

/** WCAG relative luminance of an `H S% L%` triplet. */
function luminance(triplet: string): number {
  const [h, s, l] = triplet.replace(/%/g, "").split(/\s+/).map(Number);
  const sat = s / 100;
  const lig = l / 100;
  const k = (n: number) => (n + h / 30) % 12;
  const a = sat * Math.min(lig, 1 - lig);
  const f = (n: number) => lig - a * Math.max(-1, Math.min(k(n) - 3, Math.min(9 - k(n), 1)));
  const lin = (c: number) => (c <= 0.03928 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4);
  return 0.2126 * lin(f(0)) + 0.7152 * lin(f(8)) + 0.0722 * lin(f(4));
}

describe("globals.css maps shadcn tokens to Tailwind v4 colours (#1337)", () => {
  it("declares an @theme inline block", () => {
    expect(themeBlock()).not.toBe("");
  });

  it.each(TOKENS)("maps --color-%s to its HSL variable", (token) => {
    const declaration = new RegExp(`--color-${token}:\\s*hsl\\(var\\(--${token}\\)\\)\\s*;`);
    expect(themeBlock()).toMatch(declaration);
  });

  it("white text on --primary meets WCAG AA 4.5:1 (brand #0071E3)", () => {
    const primary = rootVar("primary");
    expect(primary).toBeDefined();
    const ratio = (1 + 0.05) / (luminance(primary!) + 0.05);
    expect(ratio).toBeGreaterThanOrEqual(4.5);
  });

  it("the focus ring follows the primary colour", () => {
    expect(rootVar("ring")).toBe(rootVar("primary"));
  });
});
