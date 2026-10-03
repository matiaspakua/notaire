/**
 * Hex hygiene for design-system compliance (#960 / CU76 / RF #78).
 * Audited globs under app/ and components/ must not embed #RRGGBB literals.
 * Excludes theme/ (token definitions) and 3-digit issue refs like #960.
 */
import { describe, it, expect } from "vitest";
import { existsSync, readdirSync, readFileSync, statSync } from "node:fs";
import { join, relative, resolve } from "node:path";

const SRC_ROOT = resolve(__dirname, "../..");
const AUDITED_ROOTS = ["app", "components"].map((dir) => join(SRC_ROOT, dir));

/** Six-digit hex color literals only — excludes issue refs like #960. */
const HEX_RRGGBB = /#(?:[0-9A-Fa-f]{6})\b/g;

const REQUIRED_FILES = [
  "app/dashboard/layout.tsx",
  "app/dashboard/page.tsx",
  "components/ui/table.tsx",
];

function isExcluded(absolutePath: string): boolean {
  const rel = relative(SRC_ROOT, absolutePath).replace(/\\/g, "/");
  if (rel.startsWith("theme/") || rel.includes("/theme/")) return true;
  return false;
}

function walkFiles(dir: string, out: string[] = []): string[] {
  if (!existsSync(dir)) return out;
  for (const entry of readdirSync(dir)) {
    const full = join(dir, entry);
    const st = statSync(full);
    if (st.isDirectory()) {
      if (entry === "theme" || entry === "node_modules" || entry === ".git") continue;
      walkFiles(full, out);
      continue;
    }
    if (!/\.(tsx?|jsx?|css)$/.test(entry)) continue;
    if (isExcluded(full)) continue;
    out.push(full);
  }
  return out;
}

function findHexHits(filePath: string): string[] {
  const text = readFileSync(filePath, "utf8");
  const hits: string[] = [];
  for (const match of text.matchAll(HEX_RRGGBB)) {
    hits.push(match[0]);
  }
  return hits;
}

describe("frontend hex hygiene (#960)", () => {
  it("requires the audited dashboard/table sources to exist", () => {
    for (const rel of REQUIRED_FILES) {
      expect(existsSync(join(SRC_ROOT, rel)), `missing ${rel}`).toBe(true);
    }
  });

  it("dashboard layout has no #RRGGBB literals", () => {
    const hits = findHexHits(join(SRC_ROOT, "app/dashboard/layout.tsx"));
    expect(hits, `layout.tsx still has hex: ${hits.join(", ")}`).toEqual([]);
  });

  it("dashboard home page has no #RRGGBB literals", () => {
    const hits = findHexHits(join(SRC_ROOT, "app/dashboard/page.tsx"));
    expect(hits, `page.tsx still has hex: ${hits.join(", ")}`).toEqual([]);
  });

  it("shared table UI has no #RRGGBB literals", () => {
    const hits = findHexHits(join(SRC_ROOT, "components/ui/table.tsx"));
    expect(hits, `table.tsx still has hex: ${hits.join(", ")}`).toEqual([]);
  });

  it("audited app/ and components/ globs (excluding theme/) stay #RRGGBB-free", () => {
    const files = AUDITED_ROOTS.flatMap((root) => walkFiles(root));
    const offenders: string[] = [];
    for (const file of files) {
      const hits = findHexHits(file);
      if (hits.length > 0) {
        offenders.push(`${relative(SRC_ROOT, file)}: ${hits.join(", ")}`);
      }
    }
    expect(offenders, `hex literals found:\n${offenders.join("\n")}`).toEqual([]);
  });
});
