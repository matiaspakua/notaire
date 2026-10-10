/**
 * One motion token system (#1368, RNF-05 / RNF-03).
 *
 * globals.css `:root --motion-*` is the CSS source; Tailwind utilities
 * (`duration-fast`, `ease-standard`...) read it, and theme/motion.ts mirrors it
 * for motion/react. Ad-hoc numeric durations and `transition: all` are banned:
 * they made dialogs, selects, buttons and pages move at different speeds and
 * animated layout properties.
 */
import { describe, it, expect } from "vitest";
import { existsSync, readdirSync, readFileSync, statSync } from "node:fs";
import { join, relative, resolve } from "node:path";
import * as motionTokens from "@/theme/motion";
import { pageVariants, fadeUpItem, staggerDelay } from "@/components/motion";

const SRC = resolve(__dirname, "../..");
const css = readFileSync(join(SRC, "app/globals.css"), "utf8");

function cssVar(name: string): string | undefined {
  return css.match(new RegExp(`--motion-${name}:\\s*([^;]+);`))?.[1].trim();
}

function walk(dir: string, out: string[] = []): string[] {
  if (!existsSync(dir)) return out;
  for (const entry of readdirSync(dir)) {
    const full = join(dir, entry);
    if (statSync(full).isDirectory()) {
      if (["node_modules", "tests", "generated"].includes(entry)) continue;
      walk(full, out);
    } else if (/\.(tsx?|css)$/.test(entry) && !/\.test\.tsx?$/.test(entry)) {
      out.push(full);
    }
  }
  return out;
}

const SOURCES = ["app", "components", "theme", "lib", "hooks"].flatMap((d) => walk(join(SRC, d)));

function offenders(pattern: RegExp): string[] {
  return SOURCES.flatMap((f) =>
    [...readFileSync(f, "utf8").matchAll(pattern)].map((m) => `${relative(SRC, f)}: ${m[0]}`),
  );
}

const DURATIONS = { instant: 0, fast: 120, base: 180, slow: 240, exit: 180, page: 160 } as const;
const EASINGS = {
  standard: [0.2, 0, 0, 1],
  emphasized: [0.3, 0, 0, 1],
  exit: [0.3, 0, 1, 1],
} as const;

describe("motion tokens (#1368)", () => {
  it.each(Object.entries(DURATIONS))("--motion-duration-%s is %sms in CSS and mirrored in theme/motion.ts", (name, ms) => {
    expect(cssVar(`duration-${name}`)).toBe(`${ms}ms`);
    expect(motionTokens.motion.duration[name as keyof typeof DURATIONS]).toBeCloseTo(ms / 1000, 5);
  });

  it.each(Object.entries(EASINGS))("--motion-ease-%s matches theme/motion.ts", (name, curve) => {
    expect(cssVar(`ease-${name}`)).toBe(`cubic-bezier(${curve.join(", ")})`);
    expect(motionTokens.motion.ease[name as keyof typeof EASINGS]).toEqual(curve);
  });

  it("exposes duration and easing utilities to Tailwind", () => {
    for (const name of ["fast", "base", "slow", "exit", "page"]) {
      expect(css).toMatch(new RegExp(`@utility duration-${name}\\s*\\{[^}]*var\\(--motion-duration-${name}\\)`));
    }
    for (const name of Object.keys(EASINGS)) {
      expect(css).toContain(`--ease-${name}: var(--motion-ease-${name});`);
    }
  });

  it("uses no numeric Tailwind durations (use duration-fast/base/slow/exit/page)", () => {
    expect(offenders(/(?<![\w-])duration-\d+\b/g)).toEqual([]);
  });

  it("uses no default Tailwind easings (use ease-standard/emphasized/exit)", () => {
    expect(offenders(/(?<![\w-])ease-(?:in|out|in-out|linear)(?![\w-])/g)).toEqual([]);
  });

  it("never transitions `all` properties", () => {
    expect(offenders(/(?<![\w-])transition-all\b|transition:\s*all\b|transition:\s*`all\b/g)).toEqual([]);
  });

  it("buttons have no hover scale; press feedback is scale(0.98) only", () => {
    const button = css.match(/\.apple-button\s*\{([^}]*)\}/)?.[1] ?? "";
    expect(button).toMatch(/transition-property:\s*background-color,\s*box-shadow,\s*transform/);
    expect(css).not.toMatch(/\.apple-button:hover\s*\{[^}]*scale/);
    expect(css).toMatch(/\.apple-button:active\s*\{[^}]*scale\(0\.98\)/);
  });

  it("the route transition is a short fade-up with no exit wait", () => {
    const visible = pageVariants.visible as { y: number; transition: { duration: number } };
    const hidden = pageVariants.hidden as { y: number };
    expect(visible.transition.duration).toBeCloseTo(DURATIONS.page / 1000, 5);
    expect(Math.abs(hidden.y)).toBeLessThanOrEqual(4);
    expect(pageVariants.exit).toBeUndefined();
    const layout = readFileSync(join(SRC, "app/dashboard/layout.tsx"), "utf8");
    expect(layout).not.toMatch(/mode="wait"/);
  });

  it("list items fade up 4px and the stagger is capped at 6 items x 30ms", () => {
    expect(Math.abs((fadeUpItem.hidden as { y: number }).y)).toBeLessThanOrEqual(4);
    expect(staggerDelay(0)).toBe(0);
    expect(staggerDelay(1)).toBeCloseTo(0.03, 5);
    expect(staggerDelay(5)).toBeCloseTo(0.15, 5);
    expect(staggerDelay(13)).toBeCloseTo(0.15, 5);
  });
});
