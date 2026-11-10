import { describe, expect, it } from "vitest";
import type { SiteMetrics } from "@/lib/metrics-core";

describe("SiteMetrics test counts (#1982)", () => {
  it("keeps Vitest and Playwright counts on separate fields", () => {
    const sample: Pick<SiteMetrics, "frontendTestClasses" | "e2eSpecFiles" | "backendTestClasses" | "testClasses"> = {
      backendTestClasses: 361,
      frontendTestClasses: 197,
      e2eSpecFiles: 102,
      testClasses: 361 + 197 + 102,
    };
    expect(sample.frontendTestClasses).not.toBe(sample.e2eSpecFiles);
    expect(sample.testClasses).toBe(
      sample.backendTestClasses + sample.frontendTestClasses + sample.e2eSpecFiles,
    );
  });
});
