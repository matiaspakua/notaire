/**
 * Filled button hover states keep 4.5:1 text contrast (#2259 audit, WCAG 1.4.3).
 *
 * `hover:bg-primary/90` lightens the brand blue towards the white page, which drops white text
 * from 4.7:1 at rest to 4.0:1 on hover. Each filled variant's hover background, blended on
 * white at its opacity, must still reach 4.5:1 against the variant's foreground.
 */
import { describe, it, expect } from "vitest";
import { readFileSync } from "node:fs";
import { resolve } from "node:path";

const css = readFileSync(resolve(__dirname, "../../app/globals.css"), "utf8");
const root = css.match(/:root\s*\{([^}]*)\}/)?.[1] ?? "";
const button = readFileSync(resolve(__dirname, "../../components/ui/button.tsx"), "utf8");

function hslVar(name: string): [number, number, number] {
  const value = root.match(new RegExp(`--${name}:\\s*([^;]+);`))?.[1].trim();
  if (!value) throw new Error(`--${name} is not declared in :root`);
  const [h, s, l] = value.replace(/%/g, "").split(/\s+/).map(Number);
  const a = (s / 100) * Math.min(l / 100, 1 - l / 100);
  const k = (n: number) => (n + h / 30) % 12;
  const f = (n: number) => l / 100 - a * Math.max(-1, Math.min(k(n) - 3, Math.min(9 - k(n), 1)));
  return [f(0), f(8), f(4)];
}

const lin = (c: number) => (c <= 0.03928 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4);
const luminance = ([r, g, b]: number[]) => 0.2126 * lin(r) + 0.7152 * lin(g) + 0.0722 * lin(b);
const contrast = (a: number[], b: number[]) => {
  const [hi, lo] = [luminance(a), luminance(b)].sort((x, y) => y - x);
  return (hi + 0.05) / (lo + 0.05);
};
const onWhite = (rgb: number[], alpha: number) => rgb.map((c) => c * alpha + (1 - alpha));

function variantClasses(name: string): string {
  const line = button.split("\n").find((l) => new RegExp(`^\\s*"?${name}"?:\\s*"`).test(l));
  if (!line) throw new Error(`variant ${name} not found in button.tsx`);
  return line;
}

const FILLED: Array<[variant: string, foreground: string]> = [
  ["default", "primary-foreground"],
  ["apple", "primary-foreground"],
  ["destructive", "destructive-foreground"],
];

describe("filled button hover backgrounds keep 4.5:1 with their text", () => {
  for (const [variant, foreground] of FILLED) {
    it(`${variant}: hover background vs ${foreground}`, () => {
      const hover = variantClasses(variant).match(/hover:bg-([a-z-]+?)(?:\/(\d+))?(?=[\s"])/);
      expect(hover, `${variant} has a hover background`).not.toBeNull();
      const token = hover![1];
      const alpha = hover![2] ? Number(hover![2]) / 100 : 1;
      const background = onWhite(hslVar(token), alpha);
      expect(contrast(hslVar(foreground), background)).toBeGreaterThanOrEqual(4.5);
    });
  }
});
