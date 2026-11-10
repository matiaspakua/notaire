import fs from "fs";
import path from "path";
import {
  coverageIsMeasured,
  formatCoveragePercent,
  type CoverageSnapshot,
  type SiteMetrics,
} from "@/lib/metrics-core";

export type { CoverageSnapshot, SiteMetrics };
export { coverageIsMeasured, formatCoveragePercent };

const DEFAULTS: SiteMetrics = {
  generatedAt: new Date().toISOString(),
  commits: 1350,
  pullRequests: 210,
  markdownFiles: 260,
  testClasses: 660,
  backendTestClasses: 361,
  frontendTestClasses: 197,
  e2eSpecFiles: 102,
  apiTestFiles: 99,
  adrCount: 31,
  useCases: 96,
  functionalRequirements: 112,
  dockerServices: 16,
  grafanaDashboards: 4,
  aiTools: 4,
  contributors: 5,
  firstCommitDate: "2014-03-27",
  coverage: {
    backendInstruction: 32,
    backendBranch: 19,
    frontendComponent: 45,
    e2eTests: 85,
    apiEndpoints: 78,
    lastUpdated: "2026-06-18",
  },
};

export function loadMetrics(): SiteMetrics {
  try {
    const filePath = path.join(process.cwd(), "public", "metrics.json");
    const raw = fs.readFileSync(filePath, "utf-8");
    const parsed = JSON.parse(raw) as Partial<SiteMetrics>;
    return {
      ...DEFAULTS,
      ...parsed,
      coverage: { ...DEFAULTS.coverage, ...(parsed.coverage ?? {}) },
    };
  } catch {
    return DEFAULTS;
  }
}
