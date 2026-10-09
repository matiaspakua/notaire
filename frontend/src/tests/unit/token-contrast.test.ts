/**
 * Text contrast of the design tokens (#1341, RNF-09, WCAG 2.1 AA 1.4.3).
 *
 * Every token used for text must reach 4.5:1 on every light background it is
 * painted on. The lighter grays (neutral[400]/[500]) are for borders and
 * disabled states only, so they are not checked here.
 */
import { describe, it, expect } from "vitest";
import { readdirSync, readFileSync, statSync } from "node:fs";
import { join, resolve } from "node:path";
import { colors, semantic } from "@/theme/tokens";

const css = readFileSync(resolve(__dirname, "../../app/globals.css"), "utf8");
const root = css.match(/:root\s*\{([^}]*)\}/)?.[1] ?? "";

function rootVar(name: string): string {
  const value = root.match(new RegExp(`--${name}:\\s*([^;]+);`))?.[1].trim();
  if (!value) throw new Error(`--${name} is not declared in :root`);
  return value;
}

const lin = (c: number) => (c <= 0.03928 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4);
const rel = (r: number, g: number, b: number) => 0.2126 * lin(r) + 0.7152 * lin(g) + 0.0722 * lin(b);

/** WCAG relative luminance of `#RRGGBB` or an `H S% L%` triplet. */
function luminance(color: string): number {
  if (color.startsWith("#")) {
    const n = parseInt(color.slice(1), 16);
    return rel(((n >> 16) & 255) / 255, ((n >> 8) & 255) / 255, (n & 255) / 255);
  }
  const [h, s, l] = color.replace(/%/g, "").split(/\s+/).map(Number);
  const a = (s / 100) * Math.min(l / 100, 1 - l / 100);
  const k = (n: number) => (n + h / 30) % 12;
  const f = (n: number) => l / 100 - a * Math.max(-1, Math.min(k(n) - 3, Math.min(9 - k(n), 1)));
  return rel(f(0), f(8), f(4));
}

export function contrast(a: string, b: string): number {
  const [hi, lo] = [luminance(a), luminance(b)].sort((x, y) => y - x);
  return (hi + 0.05) / (lo + 0.05);
}

const LIGHT_BACKGROUNDS = {
  "neutral[0]": colors.neutral[0],
  "neutral[50]": colors.neutral[50],
  "neutral[100]": colors.neutral[100],
  // DataTable header row: bg-secondary/50 over the white card.
  "table header #F5F5F6": "#F5F5F6",
};

const TEXT_TOKENS = {
  "neutral[600]": colors.neutral[600],
  "neutral[700]": colors.neutral[700],
  "neutral[900]": colors.neutral[900],
  "form.labelSecondary": semantic.form.labelSecondary,
  "form.helperText": semantic.form.helperText,
  "form.inputPlaceholder": semantic.form.inputPlaceholder,
};

describe("tokens.ts text colours meet 4.5:1 on light backgrounds (#1341)", () => {
  for (const [text, fg] of Object.entries(TEXT_TOKENS)) {
    for (const [bgName, bg] of Object.entries(LIGHT_BACKGROUNDS)) {
      it(`${text} on ${bgName}`, () => {
        expect(contrast(fg, bg)).toBeGreaterThanOrEqual(4.5);
      });
    }
  }
});

describe("globals.css text variables meet 4.5:1 (#1341)", () => {
  const pairs: Array<[string, string]> = [
    ["muted-foreground", "background"],
    ["muted-foreground", "card"],
    ["muted-foreground", "muted"],
    ["muted-foreground", "secondary"],
    ["sidebar-muted", "sidebar"],
    ["sidebar-muted", "sidebar-hover"],
    ["destructive", "card"],
    ["destructive", "background"],
    ["destructive-foreground", "destructive"],
    ["primary-text", "background"],
    ["primary-text", "card"],
    ["primary-text", "secondary"],
  ];
  it.each(pairs)("--%s on --%s", (fg, bg) => {
    expect(contrast(rootVar(fg), rootVar(bg))).toBeGreaterThanOrEqual(4.5);
  });

  it("primary-text also holds on the #F5F5F7 page gray", () => {
    expect(contrast(rootVar("primary-text"), "#F5F5F7")).toBeGreaterThanOrEqual(4.5);
  });

  it("primary-text is registered as a Tailwind colour", () => {
    expect(css).toMatch(/--color-primary-text:\s*hsl\(var\(--primary-text\)\)\s*;/);
  });
});

describe("no light-gray text utilities in app code (#1341)", () => {
  const src = resolve(__dirname, "../..");
  const files: string[] = [];
  const walk = (dir: string) => {
    for (const name of readdirSync(dir)) {
      const p = join(dir, name);
      if (name === "tests" || name === "node_modules") continue;
      if (statSync(p).isDirectory()) walk(p);
      else if (/\.tsx?$/.test(name)) files.push(p);
    }
  };
  walk(join(src, "app"));
  walk(join(src, "components"));

  it("text-{neutral,gray,slate,zinc}-{300,400} (below 3:1 on white) is not used for text", () => {
    const offenders = files.flatMap((f) =>
      readFileSync(f, "utf8")
        .split("\n")
        .map((line, i) => ({ line, i }))
        .filter(({ line }) => /(?<![:\w-])text-(neutral|gray|slate|zinc)-(300|400)\b/.test(line))
        .map(({ i }) => `${f.slice(src.length + 1)}:${i + 1}`),
    );
    expect(offenders).toEqual([]);
  });

  it("the DataTable header uses the muted-foreground token, not an inline gray", () => {
    const table = readFileSync(join(src, "components/ui/table.tsx"), "utf8");
    expect(table).not.toMatch(/neutral\[600\]/);
  });
});
