import fs from "fs";
import path from "path";

export interface CoverageSnapshot {
  backendInstruction: number;
  backendBranch: number;
  frontendComponent: number;
  e2eTests: number;
  apiEndpoints: number;
  lastUpdated: string;
}

export interface SiteMetrics {
  generatedAt: string;
  commits: number;
  pullRequests: number;
  markdownFiles: number;
  testClasses: number;
  backendTestClasses: number;
  frontendTestClasses: number;
  /** Playwright specs under testing/e2e (issue #1982). */
  e2eSpecFiles: number;
  apiTestFiles: number;
  adrCount: number;
  useCases: number;
  functionalRequirements: number;
  dockerServices: number;
  grafanaDashboards: number;
  aiTools: number;
  contributors: number;
  firstCommitDate: string;
  coverage: CoverageSnapshot;
}

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

/** True when a coverage snapshot was present. "unknown" means the generator found no JaCoCo file and must not be read as 0%. */
export function coverageIsMeasured(coverage: CoverageSnapshot): boolean {
  return coverage.lastUpdated !== "unknown" && coverage.lastUpdated.trim() !== "";
}

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
