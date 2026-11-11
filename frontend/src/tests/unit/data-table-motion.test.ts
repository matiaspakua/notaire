/**
 * DataTable row entrance must use shared motion tokens (#2070 / RNF-05).
 * Fails if the legacy hardcoded 0.3s / Material ease returns.
 */
import { describe, it, expect } from "vitest";
import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { motion } from "@/theme/motion";
import { staggerDelay } from "@/components/motion";
import { rowTransition } from "@/components/shared/DataTable";

const MATERIAL_EASE = [0.4, 0, 0.2, 1] as const;
const SOURCE = readFileSync(
  resolve(__dirname, "../../components/shared/DataTable.tsx"),
  "utf8",
);

describe("DataTable row motion (#2070)", () => {
  it("rowTransition uses duration.base, ease.standard, and staggerDelay", () => {
    const t0 = rowTransition(0);
    const t5 = rowTransition(5);
    const t13 = rowTransition(13);

    expect(t0.duration).toBe(motion.duration.base);
    expect(t0.ease).toEqual(motion.ease.standard);
    expect(t0.delay).toBe(staggerDelay(0));
    expect(t5.delay).toBe(staggerDelay(5));
    expect(t13.delay).toBe(staggerDelay(13));

    // Legacy hardcoded values must not return.
    expect(t0.duration).not.toBe(0.3);
    expect(t0.ease).not.toEqual(MATERIAL_EASE);
    expect(t13.delay).not.toBe(0.3);
  });

  it("row entrance rise uses motion.distance.sm (not 6px)", () => {
    expect(SOURCE).toMatch(/y:\s*motion\.distance\.sm|y:\s*tokens\.distance\.sm/);
    expect(SOURCE).not.toMatch(/initial=\{\{\s*opacity:\s*0,\s*y:\s*6\s*\}\}/);
  });

  it("does not hardcode 0.3s duration or Material ease in source", () => {
    expect(SOURCE).not.toMatch(/duration:\s*0\.3\b/);
    expect(SOURCE).not.toMatch(/\[\s*0\.4\s*,\s*0\s*,\s*0\.2\s*,\s*1\s*\]/);
    expect(SOURCE).not.toMatch(/Math\.min\(\s*i\s*\*\s*0\.03\s*,\s*0\.3\s*\)/);
  });
});
