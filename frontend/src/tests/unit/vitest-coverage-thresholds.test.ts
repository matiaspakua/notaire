import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { describe, expect, it } from "vitest";

/**
 * Raise-only Vitest coverage floor guard (issue #976, CU76).
 * Thresholds in vitest.config.ts must stay at or above these documented
 * floors (mirrors backend JaCoCo ratchet). Lowering requires ADR/exception.
 *
 * Floors set 2026-10-03 on origin/main @ 68dc2cac after re-measure:
 * Statements 15.09% / Branches 10.52% / Functions 11.97% / Lines 15.55%.
 */
export const DOCUMENTED_VITEST_COVERAGE_FLOORS = {
  statements: 14,
  branches: 9,
  functions: 10,
  lines: 14,
} as const;

function readConfiguredThresholds(): Record<keyof typeof DOCUMENTED_VITEST_COVERAGE_FLOORS, number> {
  const configPath = resolve(__dirname, "../../../vitest.config.ts");
  const source = readFileSync(configPath, "utf8");
  const match = source.match(
    /thresholds\s*:\s*\{\s*statements:\s*(\d+)\s*,\s*branches:\s*(\d+)\s*,\s*functions:\s*(\d+)\s*,\s*lines:\s*(\d+)\s*,?\s*\}/,
  );
  expect(match, "vitest.config.ts must declare coverage.thresholds").not.toBeNull();
  return {
    statements: Number(match![1]),
    branches: Number(match![2]),
    functions: Number(match![3]),
    lines: Number(match![4]),
  };
}

describe("Vitest coverage thresholds raise-only floor (issue #976)", () => {
  it("keeps configured thresholds at or above the documented floors", () => {
    const configured = readConfiguredThresholds();

    expect(configured.statements).toBeGreaterThanOrEqual(
      DOCUMENTED_VITEST_COVERAGE_FLOORS.statements,
    );
    expect(configured.branches).toBeGreaterThanOrEqual(
      DOCUMENTED_VITEST_COVERAGE_FLOORS.branches,
    );
    expect(configured.functions).toBeGreaterThanOrEqual(
      DOCUMENTED_VITEST_COVERAGE_FLOORS.functions,
    );
    expect(configured.lines).toBeGreaterThanOrEqual(DOCUMENTED_VITEST_COVERAGE_FLOORS.lines);
  });

  it("documents raise-only policy in vitest.config.ts comments", () => {
    const configPath = resolve(__dirname, "../../../vitest.config.ts");
    const source = readFileSync(configPath, "utf8");
    expect(source).toMatch(/raise-only|never lower/i);
    expect(source).toMatch(/#976|2026-10-03/);
  });
});
