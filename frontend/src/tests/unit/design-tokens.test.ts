/**
 * One design-token source (#1365, RNF-05 / RNF-09, CU76).
 *
 * globals.css `:root` is the single runtime source of colour; Tailwind v4
 * `@theme` maps it to utilities (#1337) and theme/tokens.ts is a typed hex
 * mirror for SVG / canvas / inline styles. Components may only use semantic
 * utilities (bg-primary, text-success, border-border...), never the raw
 * Tailwind palette (bg-blue-50, from-emerald-500...), so a brand or status
 * colour changes in one place.
 *
 * Owner decision 2026-10-09: brand primary #0071E3; the half-built dark mode
 * is removed until it is designed and tracked separately.
 */
import { describe, it, expect } from "vitest";
import { existsSync, readdirSync, readFileSync, statSync } from "node:fs";
import { join, relative, resolve } from "node:path";
import { colors } from "@/theme/tokens";

const SRC = resolve(__dirname, "../..");
const css = readFileSync(join(SRC, "app/globals.css"), "utf8");
const root = css.match(/:root\s*\{([^}]*)\}/)?.[1] ?? "";
const themeBlock = css.match(/@theme inline\s*\{([^}]*)\}/)?.[1] ?? "";

const PALETTE =
  "slate|gray|zinc|neutral|stone|red|orange|amber|yellow|lime|green|emerald|teal|cyan|sky|blue|indigo|violet|purple|fuchsia|pink|rose";
const PREFIX =
  "bg|text|border|from|to|via|ring|ring-offset|fill|stroke|outline|divide|shadow|decoration|placeholder|accent|caret";
/** e.g. `bg-blue-50`, `hover:text-red-600`, `shadow-blue-500/10`. */
const RAW_PALETTE = new RegExp(`(?<![\\w-])(?:${PREFIX})-(?:${PALETTE})-\\d{2,3}\\b`, "g");
/** Semantic tokens have no numeric scale: `ring-primary-300` compiles to nothing. */
const SCALED_SEMANTIC = new RegExp(
  `(?<![\\w-])(?:${PREFIX})-(?:primary|secondary|destructive|muted|accent|success|warning|info)-\\d{2,3}\\b`,
  "g",
);
const ARBITRARY_HEX = /\[#[0-9a-fA-F]{3,8}\]/g;

function walk(dir: string, out: string[] = []): string[] {
  if (!existsSync(dir)) return out;
  for (const entry of readdirSync(dir)) {
    const full = join(dir, entry);
    if (statSync(full).isDirectory()) {
      if (["node_modules", "tests", "theme", "generated"].includes(entry)) continue;
      walk(full, out);
    } else if (/\.(tsx?|css)$/.test(entry) && !/\.test\.tsx?$/.test(entry)) {
      out.push(full);
    }
  }
  return out;
}

const SOURCES = ["app", "components", "lib", "hooks"].flatMap((d) => walk(join(SRC, d)));

function offenders(pattern: RegExp): string[] {
  const hits: string[] = [];
  for (const file of SOURCES) {
    const text = readFileSync(file, "utf8");
    for (const m of text.matchAll(pattern)) hits.push(`${relative(SRC, file)}: ${m[0]}`);
  }
  return hits;
}

function rootVar(name: string): string | undefined {
  return root.match(new RegExp(`--${name}:\\s*([^;]+);`))?.[1].trim();
}

/** `H S% L%` triplet → `#RRGGBB`. */
function hslToHex(triplet: string): string {
  const [h, s, l] = triplet.replace(/%/g, "").split(/\s+/).map(Number);
  const a = (s / 100) * Math.min(l / 100, 1 - l / 100);
  const f = (n: number) => {
    const k = (n + h / 30) % 12;
    const c = l / 100 - a * Math.max(-1, Math.min(k - 3, 9 - k, 1));
    return Math.round(c * 255);
  };
  return `#${[f(0), f(8), f(4)].map((v) => v.toString(16).padStart(2, "0")).join("")}`.toUpperCase();
}

function channelDistance(a: string, b: string): number {
  const pa = parseInt(a.slice(1), 16);
  const pb = parseInt(b.slice(1), 16);
  return Math.max(...[16, 8, 0].map((s) => Math.abs(((pa >> s) & 255) - ((pb >> s) & 255))));
}


const lin = (c: number) => (c <= 0.03928 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4);
function luminance(hex: string): number {
  const n = parseInt(hex.slice(1), 16);
  const [r, g, b] = [16, 8, 0].map((s) => ((n >> s) & 255) / 255);
  return 0.2126 * lin(r) + 0.7152 * lin(g) + 0.0722 * lin(b);
}
function contrast(a: string, b: string): number {
  const [hi, lo] = [luminance(a), luminance(b)].sort((x, y) => y - x);
  return (hi + 0.05) / (lo + 0.05);
}
/** `bg-x/10` painted over white. */
function tint(hex: string, alpha: number): string {
  const n = parseInt(hex.slice(1), 16);
  const mixed = [16, 8, 0].map((s) => Math.round(((n >> s) & 255) * alpha + 255 * (1 - alpha)));
  return `#${mixed.map((v) => v.toString(16).padStart(2, "0")).join("")}`;
}

describe("design tokens: one source (#1365)", () => {
  it("has no dark-mode block or dark: utilities (owner decision: removed for now)", () => {
    expect(css).not.toMatch(/\.dark\s*\{/);
    expect(offenders(/(?<![\w-])dark:[\w-]+/g)).toEqual([]);
  });

  it.each(["success", "warning", "info"])("declares --%s and its foreground and maps them in @theme", (name) => {
    expect(rootVar(name), `--${name} missing in :root`).toBeDefined();
    expect(rootVar(`${name}-foreground`), `--${name}-foreground missing in :root`).toBeDefined();
    expect(themeBlock).toContain(`--color-${name}: hsl(var(--${name}));`);
    expect(themeBlock).toContain(`--color-${name}-foreground: hsl(var(--${name}-foreground));`);
  });

  it.each([
    ["primary", () => colors.primary[600]],
    ["ring", () => colors.primary[600]],
    ["destructive", () => colors.error[700]],
    ["success", () => colors.success[700]],
    ["warning", () => colors.warning[700]],
    ["info", () => colors.info[700]],
  ] as const)("tokens.ts mirrors --%s from globals.css", (name, hex) => {
    const value = rootVar(name);
    expect(value, `--${name} missing in :root`).toBeDefined();
    expect(channelDistance(hslToHex(value!), hex()), `--${name} ${hslToHex(value!)} vs tokens.ts ${hex()}`).toBeLessThanOrEqual(2);
  });

  it.each(["success", "warning", "info", "destructive"])(
    "--%s is readable as text on white and on its own /10 tint, and under white text (WCAG AA 4.5:1)",
    (name) => {
      const value = rootVar(name);
      expect(value, `--${name} missing in :root`).toBeDefined();
      const hex = hslToHex(value!);
      expect(contrast(hex, "#FFFFFF")).toBeGreaterThanOrEqual(4.5);
      expect(contrast(hex, tint(hex, 0.1))).toBeGreaterThanOrEqual(4.5);
      const fg = rootVar(`${name}-foreground`);
      expect(fg, `--${name}-foreground missing in :root`).toBeDefined();
      expect(contrast(hslToHex(fg!), hex)).toBeGreaterThanOrEqual(4.5);
    },
  );

  it("defines the brand hex #0071E3 exactly once (tokens.ts) and never the old #0080FF", () => {
    const files = [...walk(join(SRC, "theme")), ...readdirSync(join(SRC, "theme")).map((f) => join(SRC, "theme", f))]
      .filter((f, i, all) => all.indexOf(f) === i && /\.tsx?$/.test(f));
    const hits = [...SOURCES, ...files].flatMap((f) =>
      [...readFileSync(f, "utf8").matchAll(/#0071E3|#0080FF/gi)].map((m) => `${relative(SRC, f)}: ${m[0]}`),
    );
    expect(hits).toEqual(["theme/tokens.ts: #0071E3"]);
  });

  it("uses no raw Tailwind palette utilities in app/, components/, lib/ or hooks/", () => {
    expect(offenders(RAW_PALETTE)).toEqual([]);
  });

  it("uses no numeric scale on semantic tokens (they generate no CSS)", () => {
    expect(offenders(SCALED_SEMANTIC)).toEqual([]);
  });

  it("uses no arbitrary [#hex] colour values", () => {
    expect(offenders(ARBITRARY_HEX)).toEqual([]);
  });
});
