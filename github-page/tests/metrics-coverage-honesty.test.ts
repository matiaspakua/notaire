import { describe, expect, it } from "vitest";
import { coverageIsMeasured, type CoverageSnapshot } from "@/lib/metrics";

describe("coverage honesty (#2203)", () => {
  it("treats unknown lastUpdated as not measured", () => {
    const coverage: CoverageSnapshot = {
      backendInstruction: null,
      backendBranch: null,
      frontendComponent: null,
      e2eTests: null,
      apiEndpoints: null,
      lastUpdated: "unknown",
    };
    expect(coverageIsMeasured(coverage)).toBe(false);
  });

  it("treats null percentages with a dated stamp as not a full measurement when stamp is unknown only", () => {
    const coverage: CoverageSnapshot = {
      backendInstruction: 0,
      backendBranch: 0,
      frontendComponent: 0,
      e2eTests: 0,
      apiEndpoints: 0,
      lastUpdated: "unknown",
    };
    expect(coverageIsMeasured(coverage)).toBe(false);
  });

  it("treats a dated JaCoCo stamp as measured", () => {
    const coverage: CoverageSnapshot = {
      backendInstruction: 87,
      backendBranch: 71,
      frontendComponent: 82,
      e2eTests: null,
      apiEndpoints: null,
      lastUpdated: "2026-11-10",
    };
    expect(coverageIsMeasured(coverage)).toBe(true);
  });
});
